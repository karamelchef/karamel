package se.kth.karamel.webservicemodel;

public class StatusResponseJSON {

  static public String SUCCESS_STRING = "success";
  static public String ERROR_STRING = "error";

  private String status;
  private String reason;

  public StatusResponseJSON(String status, String reason) {
    this.status = status;
    this.reason = reason;
  }

  /**
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * @return the reason
   */
  public String getReason() {
    return reason;
  }

  /**
   * @param reason the reason to set
   */
  public void setReason(String reason) {
    this.reason = reason;
  }
}
