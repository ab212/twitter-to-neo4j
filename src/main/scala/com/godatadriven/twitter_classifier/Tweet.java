package com.godatadriven.twitter_classifier;

import com.google.gson.annotations.SerializedName;

public class Tweet {
  @SerializedName("id")
  private String tweetId;

  @SerializedName("text")
  private String text;

  @SerializedName("inReplyToScreenName")
  private String inReplyToScreenName;

  @SerializedName("user")
  private User user;

  @SerializedName("source")
  private String source;

  @SerializedName("userMentionEntities")
  private User[] userMentionEntities;

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
