# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

hello-NewsBot is a Spring Boot app that fetches IT news from RSS feeds on a daily schedule (08:00 Asia/Seoul) and sends new articles to each subscribed user's own Telegram chat. Visitors link their Telegram account via a bot deep-link (no password/email auth), pick which outlets they want from a small web frontend, and get only the outlets they subscribed to. Sent-article history is tracked per user (keyed by `(user, link)`) to avoid duplicate sends.

Note: `hello.hello_NewsBot` uses an underscore because the original package name `hello.hello-NewsBot` was invalid Java (hyphens not allowed in package names) — see HELP.md.

## Common Commands

```bash
# Build (Windows)
./gradlew.bat build

# Run tests
./gradlew.bat test

# Run a single test class
./gradlew.bat test --tests "hello.hello_NewsBot.HelloNewsBotApplicationTests"

# Run the app locally
./gradlew.bat bootRun
```

Java 25 toolchain is required (configured via Gradle toolchain in build.gradle).

### Required environment variables

- `BOT_TOKEN` — Telegram bot token, from @BotFather (required, no default)
- `BOT_USERNAME` — the bot's `@`-less username, used to build the `t.me/<username>?start=<code>` deep link shown on the linking page (required, no default)
- `DATABASE_URL` / `DATABASE_USERNAME` / `DATABASE_PASSWORD` / `DATABASE_DRIVER` — optional; default to a local H2 file DB at `./data/newsdb`. In production (Render), these are overridden to point at PostgreSQL.
- `PORT` — HTTP port (defaults to 8080)

There is no `CHAT_ID` env var anymore — recipients are per-user (`User.telegramChatId`), not a single fixed chat.

## Architecture

The app has two independent flows: **account linking** (web ↔ Telegram, via webhook) and **daily delivery** (scheduler → per-user send). Both hang off the same `User` entity.

### Account linking (web signup + Telegram webhook)

```
browser: "텔레그램으로 시작하기" click
  → POST /link/start (LinkController) → TelegramLinkService.startLink()
      creates a User row with a random linkCode, chat id still null
  → browser opens https://t.me/<BOT_USERNAME>?start=<linkCode>
  → user presses START in their own Telegram app → Telegram sends "/start <linkCode>"
      to our registered webhook URL
  → POST /telegram/webhook (TelegramWebhookController) → TelegramLinkService.handleIncomingMessage()
      looks up the User by linkCode, fills in telegramChatId → linking complete
  → browser was polling GET /link/status?linkCode=... every 2s; once it reports
      connected:true, it stores userId and redirects to the feed-picker page
```

- **domain/User** — id, `linkCode` (temporary, pre-connection), `telegramChatId` (null until linked), `createdAt`. Table name is explicitly `users` (`@Table(name = "users")`) — the bare entity name `user` collides with a reserved word in H2/most SQL dialects.
- **service/TelegramLinkService** — owns both halves of linking: issuing a `linkCode` and consuming the webhook's `/start <linkCode>` message.
- **dto/TelegramUpdate, TelegramMessage, TelegramChat** — minimal Jackson DTOs for Telegram's webhook payload shape (`update.message.text`, `update.message.chat.id`); `@JsonIgnoreProperties(ignoreUnknown = true)` because Telegram sends many more fields than we need.
- **controller/LinkController** — `POST /link/start` issues the deep link; `GET /link/status?linkCode=...` is polled by the frontend since the webhook completes the link asynchronously and the browser has no other way to know it happened.
- **controller/TelegramWebhookController** — `POST /telegram/webhook`, the endpoint Telegram calls. Only receives traffic after `setWebhook` has been registered against a **publicly reachable** URL — `localhost` doesn't work, since Telegram's servers are what initiate the request. Use `ngrok http 8080` for local testing (`ngrok http 8080` → `setWebhook` with the printed `https://*.ngrok-free.dev/telegram/webhook` URL → `deleteWebhook` when done); in production the Render URL is used directly.

### Feed subscription

- **domain/UserFeedSubscription** — join row: `(User, feedName)`. The set of *available* feeds still lives in `application.yml` (`FeedConfig`); this table only stores which of those a given user picked.
- **service/FeedSubscriptionService** — `listAvailableFeeds()` reads straight from `FeedConfig`; `updateSubscriptions(user, feedNames)` replaces a user's whole subscription set (delete-then-insert, not a diff) inside a transaction.
- **controller/FeedSubscriptionController** — `GET /feeds` (candidates), `GET /users/{userId}/feeds` / `PUT /users/{userId}/feeds` (a user's picks). `userId` is taken straight from the path — there's no session/auth layer yet, so the frontend just remembers `userId` in `localStorage` after linking.

### Daily delivery (per user)

```
NewsScheduler (cron, daily 08:00 KST) or HealthController (GET /trigger, manual)
  → NewsDeliveryService.deliverDailyNews()
      for each User with a non-null telegramChatId:
        subscribed feed names (FeedSubscriptionService)
          → resolve to FeedConfig.Feed, fetch via NewsFetchService.fetchArticles(feed)
            (cached per feed per run — a feed shared by N subscribers is only fetched once)
          → drop articles already sent to *this* user (SentArticleRepository.existsByUserAndLink)
          → TelegramService.send(user.telegramChatId, message) → persist new SentArticle rows
            (only after a successful send, so a failed send is retried next run)
```

- **config/FeedConfig, config/TelegramConfig** — `@ConfigurationProperties` classes bound to the `news.*` and `telegram.*` keys in `application.yml`. `FeedConfig` holds the list of candidate RSS feeds (name + url) and `countPerFeed`. `TelegramConfig` holds `botToken` and `botUsername` (no more fixed `chatId`).
- **service/NewsFetchService** — `fetchArticles(feed)` fetches and parses a single feed, returning `List.of()` (not throwing) on failure, so one bad feed doesn't take down a user's whole delivery. Feeds are parsed manually through JDOM's `SAXBuilder` rather than ROME's built-in `XmlReader`, because some Korean outlets' feeds (e.g. 전자신문) have non-standard `DOCTYPE` declarations that break standard parsing — the raw XML is fetched as text first and the `DOCTYPE` line is stripped via regex before building the `SyndFeed`.
- **service/NewsDeliveryService** — orchestrates the per-user loop described above; keeps a `Map<String, List<Article>> articleCache` for the duration of one `deliverDailyNews()` run so a feed shared across subscribers is fetched once, not once per subscriber.
- **service/TelegramService** — `send(chatId, text)` sends one HTML-formatted message via the Telegram Bot API using `RestTemplate`; the recipient is passed in by the caller rather than read from config. Article titles are HTML-escaped (`HtmlUtils.htmlEscape`) before being embedded in the message.
- **repository/SentArticleRepository** — `existsByUserAndLink(user, link)` is the per-user dedupe check.
- **domain/Article** — plain (non-entity) DTO for a fetched RSS item (source, title, link).
- **domain/SentArticle** — JPA entity; `(user_id, link)` is a unique constraint (not the PK — PK is a generated `id`), recording that a given user has already received a given link.
- **scheduler/NewsScheduler** — `@Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")`; the zone is pinned explicitly so schedule timing doesn't depend on the deployment host's system timezone.
- **controller/HealthController** — `GET /` health check (used by Render); `GET /trigger` manually fires the same delivery flow as the scheduler (useful for testing without waiting for 08:00).

### Frontend

Plain static HTML/JS under `src/main/resources/static/` — no template engine, since the backend is already REST. Three pages calling the endpoints above: `index.html` (start linking, poll `/link/status`) → `feeds.html` (checkboxes over `GET /feeds` + `GET/PUT /users/{id}/feeds`) → `done.html` (confirmation). Since there's no auth yet, `userId` is stashed in `localStorage` after linking and reused on return visits. Note `GET /` is already claimed by `HealthController`'s health check, so the landing page is served at `/index.html`, not `/`.

## Deployment

Two-stage `Dockerfile`: builds with `eclipse-temurin:25-jdk` via `./gradlew bootJar -x test`, then runs the resulting jar on `eclipse-temurin:25-jre`. Deployed to Render (per Dockerfile/HELP.md comments), which supplies `PORT` and the Postgres `DATABASE_*` env vars at runtime; local dev falls back to file-based H2.

After each deploy to a new host/URL, the Telegram webhook must be (re-)registered once, since Telegram calls back to whatever URL was last registered via `setWebhook`:

```
curl "https://api.telegram.org/bot<BOT_TOKEN>/setWebhook" -d "url=https://<render-app>.onrender.com/telegram/webhook"
```

## Commenting convention

The project owner is using this codebase to learn the architecture, not just to ship features — so code should stay self-explanatory even out of context. Comment liberally, in Korean:

- Every class gets a comment above it explaining its role in the overall flow (what it is, why it exists).
- Every non-trivial field/method gets an inline comment explaining what it holds or does, especially if its purpose isn't obvious from the name alone.
- This applies to new code going forward, not just existing files — don't skip comments to save time.
