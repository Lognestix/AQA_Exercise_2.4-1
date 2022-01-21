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

  public static VerificationCode getVerificationCodeFor(AuthInfo authInfo) {
    return new VerificationCode("12345");
  }
}