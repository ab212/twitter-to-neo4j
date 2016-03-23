package com.godatadriven.twitter_classifier;

import com.google.gson.annotations.SerializedName;

public class Tweet {
  @SerializedName("id")
  public String tweetId;

  @SerializedName("text")
  public String text;

  @SerializedName("inReplyToScreenName")
  public String inReplyToScreenName;

  @SerializedName("user")
  public User user;

  @SerializedName("source")
  public String source;

  @SerializedName("userMentionEntities")
  public User[] userMentionEntities;

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getInReplyToScreenName() {
    return inReplyToScreenName;
  }

  public void setInReplyToScreenName(String inReplyToScreenName) {
    this.inReplyToScreenName = inReplyToScreenName;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public User[] getUserMentionEntities() {
    return userMentionEntities;
  }

  public void setUserMentionEntities(User[] userMentionEntities) {
    this.userMentionEntities = userMentionEntities;
  }

  public String getTweetId() {
    return tweetId;
  }

  public void setTweetId(String tweetId) {
    this.tweetId = tweetId;
  }
}
