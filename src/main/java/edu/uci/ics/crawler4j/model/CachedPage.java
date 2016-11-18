package edu.uci.ics.crawler4j.model;

import java.io.Serializable;

import org.joda.time.DateTime;

import edu.uci.ics.crawler4j.url.WebURL;

public class CachedPage implements Serializable {
    private String url;
    private String canonicalUrl;
    private DateTime lastModifiedDate;
    private WebURL webURL;

    public CachedPage(String url, DateTime lastModifiedDate, WebURL webURL) {
        this.url = url;
        this.canonicalUrl = url;
        this.lastModifiedDate = lastModifiedDate;
        this.webURL = webURL;
    }

    public CachedPage(String url, String canonicalUrl, DateTime lastModifiedDate, WebURL webURL) {
        this.url = url;
        this.canonicalUrl = canonicalUrl;
        this.lastModifiedDate = lastModifiedDate;
        this.webURL = webURL;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    public DateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(DateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public WebURL getWebURL() {
        return webURL;
    }

    public void setWebURL(WebURL webURL) {
        this.webURL = webURL;
    }
}
