package model;

import twitter4j.HashtagEntity;

import java.util.Date;

public class Tweet {
    public long id;
    public String hashtag;
    public String language;
    public String content;
    public Date date;

    public Tweet(long id, String hashtag, String language, String content, Date date) {
        this.id = id;
        this.hashtag = hashtag;
        this.language = language;
        this.date = date;
        content = content.replace(')', '}');
        content = content.replace('(', '{');
        content = content.replace('\'', '’');
        this.content = content;
    }

    static public String hashtagString(HashtagEntity[] hashTags){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hashTags.length; i++){
            sb.append(hashTags[i].getText()).append(", ");
        }
        return sb.toString();
    }

    public long getId() {
        return id;
    }

    public String getHashtag() {
        return hashtag;
    }

    public String getLanguage() {
        return language;
    }

    public String getContent() {
        return content;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "id=" + id +
                ", hashtag='" + hashtag + '\'' +
                ", language='" + language + '\'' +
                ", content='" + content + '\'' +
                ", date=" + date +
                '}';
    }
}