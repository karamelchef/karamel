/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.webservicemodel;

import java.util.Date;

/**
 *
 * @author alidar
 */
public class NodeGroupJSON {

  private String fileNamePattern;
  private int minFileSize;
  private int maxFileSize;
  private Date minUploadDate;
  private Date maxUploadDate;
  private String language;
  private String descriptionPattern;

  /**
   * @return the fileNamePattern
   */
  public String getFileNamePattern() {
    return fileNamePattern;
  }

  /**
   * @param fileNamePattern the fileNamePattern to set
   */
  public void setFileNamePattern(String fileNamePattern) {
    this.fileNamePattern = fileNamePattern;
  }

  /**
   * @return the minFileSize
   */
  public int getMinFileSize() {
    return minFileSize;
  }

  /**
   * @param minFileSize the minFileSize to set
   */
  public void setMinFileSize(int minFileSize) {
    this.minFileSize = minFileSize;
  }

  /**
   * @return the maxFileSize
   */
  public int getMaxFileSize() {
    return maxFileSize;
  }

  /**
   * @param maxFileSize the maxFileSize to set
   */
  public void setMaxFileSize(int maxFileSize) {
    this.maxFileSize = maxFileSize;
  }

  /**
   * @return the minUploadDate
   */
  public Date getMinUploadDate() {
    return minUploadDate;
  }

  /**
   * @param minUploadDate the minUploadDate to set
   */
  public void setMinUploadDate(Date minUploadDate) {
    this.minUploadDate = minUploadDate;
  }

  /**
   * @return the maxUploadDate
   */
  public Date getMaxUploadDate() {
    return maxUploadDate;
  }

  /**
   * @param maxUploadDate the maxUploadDate to set
   */
  public void setMaxUploadDate(Date maxUploadDate) {
    this.maxUploadDate = maxUploadDate;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * @return the descriptionPattern
   */
  public String getDescriptionPattern() {
    return descriptionPattern;
  }

  /**
   * @param descriptionPattern the descriptionPattern to set
   */
  public void setDescriptionPattern(String descriptionPattern) {
    this.descriptionPattern = descriptionPattern;
  }
}
