package ru.netology.data;

import lombok.Value;

public class DataHelper {
  private DataHelper() {}

  @Value  //Дата класс AuthInfo и его методы
  public static class AuthInfo {
    private String login;
    private String password;
  }

  public static AuthInfo getUserAuthInfo() {
    return new AuthInfo("vasya", "qwerty123");
  }

  @Value  //Дата класс VerificationCode и его методы
  public static class VerificationCode {
    private String code;
  }

  public static VerificationCode getVerificationCodeFor() {
    return new VerificationCode("12345");
  }

  @Value  //Дата класс CardId и его методы
  public static class CardId {
    private String id;
  }

  public static CardId getFirstCardId() {
    return new CardId("92df3f1c-a033-48e6-8390-206f6b1f56c0");
  }

  public static CardId getSecondCardId() {
    return new CardId("0f3f5c2a-249e-4c3d-8287-09f7a039391d");
  }

  @Value
  public static class TransferInfo {
    private int amount;
    private String numberCard;
  }

  public static TransferInfo getFirstCardTransferInfoPositive() {
    return new TransferInfo(7_401, "5559 0000 0000 0002");
  }

  public static TransferInfo getSecondCardTransferInfoPositive() {
    return new TransferInfo(4_068, "5559 0000 0000 0001");
  }

  public static TransferInfo getFirstCardTransferInfoNegative() {
    return new TransferInfo(-906, "5559 0000 0000 0002");
  }

  public static TransferInfo getSecondCardTransferInfoNegative() {
    return new TransferInfo(21_739, "5559 0000 0000 0001");
  }

  public static TransferInfo setFirstCardTransferInfo(int amountTransfer) {
    return new TransferInfo(amountTransfer, "5559 0000 0000 0002");
  }

  public static TransferInfo setSecondCardTransferInfo(int amountTransfer) {
    return new TransferInfo(amountTransfer, "5559 0000 0000 0001");
  }
}