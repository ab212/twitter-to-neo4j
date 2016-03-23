package com.godatadriven.twitter_classifier;

import com.google.gson.annotations.SerializedName;

public class User {
  @SerializedName("screenName")
  private String screenName;

  @SerializedName("lang")
  private String language;

  public String getScreenName() {
    return screenName;
  }

  public void setScreenName(String screenName) {
    this.screenName = screenName;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
}
