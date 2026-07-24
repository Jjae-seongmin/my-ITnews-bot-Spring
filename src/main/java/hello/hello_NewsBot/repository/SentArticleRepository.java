package hello.hello_NewsBot.repository;

import hello.hello_NewsBot.domain.SentArticle;
import org.springframework.data.jpa.repository.JpaRepository;

// sent.json 파일
public interface SentArticleRepository extends JpaRepository<SentArticle, String> {
    // existsById(link)를 기본 제공받음 — Python의 "link in sent_set"과 같은 역할
}
