package cn.com.shopgroup.user.http.response;

import cn.com.shopgroup.user.model.GbArticleInfo;
import lombok.Data;

@Data
public class ArticleResponse {

    private Long id;
    private String title;
    private String content;

    public ArticleResponse(){

    }

    public ArticleResponse(GbArticleInfo data){
        this.id = data.getArticleId();
        this.title = data.getArticleTitle();
        this.content = data.getArticleContent();
    }

}
