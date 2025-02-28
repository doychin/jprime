package site.model;

import java.io.Serial;
import java.util.Collection;
import java.util.HashSet;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class Article extends AbstractEntity {

    /**
     * Default serial version uid.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private String title;

    private String description;

    private boolean published;

    @NotNull
    @Lob
    @Column(length = 10000)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
    @JoinColumn(name = "author", referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "fk_article_author"))
    private User author;

    //Changed to eager, session problems! TODO:rethink!
    @ManyToMany(fetch = FetchType.EAGER, targetEntity = Tag.class)
    @JoinTable(name = "tags_articles",
        joinColumns = @JoinColumn(name = "article_pk", foreignKey = @ForeignKey(name = "fk_tag_article")),
        inverseJoinColumns = @JoinColumn(name = "tag_pk", foreignKey = @ForeignKey(name = "fk_article_tag")),
        indexes = {
            @Index(columnList = "article_pk")
        })
    private Collection<Tag> tags = new HashSet<>();

    public Article() {
    }

    public Article(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Collection<Tag> getTags() {
        return tags;
    }

    public void setTags(Collection<Tag> tags) {
        this.tags = tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isPublished() {
        return published;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Article article)) {
            return false;
        }

        return title.equals(article.title);
    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }
}

