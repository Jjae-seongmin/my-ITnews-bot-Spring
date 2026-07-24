package hello.hello_NewsBot.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

// RSS 에서 뽑아낸 기사 하나.
@Getter
@AllArgsConstructor
public class Article {
    private String source;
    private String title;
    private String link;
}