## `Статус сборки` [![Build status](https://ci.appveyor.com/api/projects/status/rdegan0naj2f4tje?svg=true)](https://ci.appveyor.com/project/Lognestix/aqa-exercise-2-3-2)
## В build.gradle добавленна поддержка JUnit-Jupiter, Selenide и headless-режим, Lombok.
```gradle
plugins {
    id 'java'
}

group 'ru.netology'
version '1.0-SNAPSHOT'

sourceCompatibility = 11

//Кодировка файлов (если используется русский язык в файлах)
compileJava.options.encoding = "UTF-8"
compileTestJava.options.encoding = "UTF-8"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly 'org.projectlombok:lombok:1.18.22'
    annotationProcessor 'org.projectlombok:lombok:1.18.22'

    testImplementation 'org.junit.jupiter:junit-jupiter:5.8.2'
    testImplementation 'com.codeborne:selenide:6.2.0'

    testCompileOnly 'org.projectlombok:lombok:1.18.22'
    testAnnotationProcessor 'org.projectlombok:lombok:1.18.22'
}

test {
    useJUnitPlatform()
    //В тестах, при вызове `gradlew test -Dselenide.headless=true` будет передаватся этот параметр в JVM (где его подтянет Selenide)
    systemProperty 'selenide.headless', System.getProperty('selenide.headless')
}
```
## Код Java для оптимизации авто-тестов.
```Java
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
```
```Java
package ru.netology.page;

import com.codeborne.selenide.SelenideElement;
import ru.netology.data.DataHelper;

import static com.codeborne.selenide.Selenide.$;

public class LoginPage {
  private final SelenideElement loginField = $("[data-test-id=login] input");
  private final SelenideElement passwordField = $("[data-test-id=password] input");
  private final SelenideElement loginButton = $("[data-test-id=action-login]");

  public VerificationPage validLogin(DataHelper.AuthInfo info) {
    loginField.setValue(info.getLogin());
    passwordField.setValue(info.getPassword());
    loginButton.click();
    return new VerificationPage();
  }
}
```
```Java
package ru.netology.page;

import com.codeborne.selenide.SelenideElement;
import ru.netology.data.DataHelper;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class VerificationPage {
  private final SelenideElement codeField = $("[data-test-id=code] input");
  private final SelenideElement verifyButton = $("[data-test-id=action-verify]");

  public VerificationPage() {
    codeField.shouldBe(visible);
  }

  public DashboardPage validVerify(DataHelper.VerificationCode verificationCode) {
    codeField.setValue(verificationCode.getCode());
    verifyButton.click();
    return new DashboardPage();
  }
}
```
```Java
package ru.netology.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Keys;
import ru.netology.data.NotFoundException;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class DashboardPage {
  private final SelenideElement heading = $("[data-test-id=dashboard]");
  private static final ElementsCollection cards = $$(".list__item");
  private static final String balanceStart = "баланс:";
  private static final String balanceFinish = "р.";
  private static final SelenideElement amountField = $("[data-test-id=amount] input");
  private static final SelenideElement fromField = $("[data-test-id=from] input");
  private static final SelenideElement actionTransferButton = $("[data-test-id=action-transfer]");

  public DashboardPage() {
    heading.shouldBe(visible);
  }

  public static int getCardBalance(String id) {
    for (SelenideElement card : cards) {
      var attributeValue = card.find("[data-test-id]").attr("data-test-id");
      assert attributeValue != null;
      if (attributeValue.equals(id)) {
        return extractBalance(card.text());
      }
    }
    throw new NotFoundException(
            "Card with id: " + id + " not found");
  }

    private static int extractBalance(String cardInfo) {
    var value = cardInfo.substring    //Вырезается нужная часть строки
            (cardInfo.indexOf(balanceStart) + balanceStart.length(),    //Начальная позиция (исключительно) плюс смещение
                    cardInfo.indexOf(balanceFinish))    //Конечная позиция (включительно)
            .trim();    //Обрезка начального и конечного пробелов
    return Integer.parseInt(value);
  }

  public static void transferBetweenOwnCards(String idCard, String numberCard, int amount) {
    for (SelenideElement card : cards) {
      var attributeValue = card.find("[data-test-id]").attr("data-test-id");
      assert attributeValue != null;
      if (attributeValue.equals(idCard)) {
        card.find("[data-test-id=action-deposit]").click();
        amountField.sendKeys(Keys.CONTROL + "a");
        amountField.sendKeys(Keys.DELETE);
        amountField.setValue(String.valueOf(amount));
        fromField.sendKeys(Keys.CONTROL + "a");
        fromField.sendKeys(Keys.DELETE);
        fromField.setValue(numberCard);
        actionTransferButton.click();
        return;
      }
    }
  }
}
```
## Авто-тесты находящиеся в этом репозитории.
```Java
import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.page.DashboardPage;
import ru.netology.page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.*;

class TransferMoneyBetweenOwnCardsTest {
  String firstIdCard = "92df3f1c-a033-48e6-8390-206f6b1f56c0";
  String secondIdCard = "0f3f5c2a-249e-4c3d-8287-09f7a039391d";
  String firstNumberCard = "5559 0000 0000 0001";
  String secondNumberCard = "5559 0000 0000 0002";

  @BeforeAll
  public static void loginToPersonalAccount() {
    //Вместо первых двух команд можно использовать эту:
    //var loginPage = open("http://localhost:9999", LoginPage.class);
    open("http://localhost:9999");
    var loginPage = new LoginPage();
    var authInfo = DataHelper.getUserAuthInfo();
    var verificationPage = loginPage.validLogin(authInfo);
    var verificationCode = DataHelper.getVerificationCodeFor(authInfo);
    verificationPage.validVerify(verificationCode);
  }

          //Позитивные проверки:
  @Test   //Перевод со второй карты на первую
  @DisplayName("Transfer money from the second card to the first card")
  public void shouldTransferFromSecondToFirst() {
    int initialBalanceFirstCard = DashboardPage.getCardBalance(firstIdCard);
    int initialBalanceSecondCard = DashboardPage.getCardBalance(secondIdCard);
    int amountTransfer = 7_401;
    DashboardPage.transferBetweenOwnCards(firstIdCard, secondNumberCard, amountTransfer);
    int finalBalanceFirstCard = DashboardPage.getCardBalance(firstIdCard);
    int finalBalanceSecondCard = DashboardPage.getCardBalance(secondIdCard);
    //Проверка зачисления на первую карту:
    assertEquals(initialBalanceFirstCard + amountTransfer, finalBalanceFirstCard);
    //Проверка списания со второй карты:
    assertEquals(initialBalanceSecondCard - amountTransfer, finalBalanceSecondCard);
  }

  @Test   //Перевод с первой карты на вторую
  @DisplayName("Transfer money from the first card to the second card")
  public void shouldTransferFromFirstToSecond() {
    int initialBalanceFirstCard = DashboardPage.getCardBalance(firstIdCard);
    int initialBalanceSecondCard = DashboardPage.getCardBalance(secondIdCard);
    int amountTransfer = 4_068;
    DashboardPage.transferBetweenOwnCards(secondIdCard, firstNumberCard, amountTransfer);
    int finalBalanceFirstCard = DashboardPage.getCardBalance(firstIdCard);
    int finalBalanceSecondCard = DashboardPage.getCardBalance(secondIdCard);
    //Проверка списания с первой карты:
    assertEquals(initialBalanceFirstCard - amountTransfer, finalBalanceFirstCard);
    //Проверка зачисления на вторую карту:
    assertEquals(initialBalanceSecondCard + amountTransfer, finalBalanceSecondCard);
  }

          //Негативные проверки:
  @Test   //Попытка перевода со второй карты на первую с отрицательной суммой перевода
  @DisplayName("Transferring money from the second card to the first card with a negative amount")
  public void shouldTransferFromSecondToFirstNegativeAmount() {
    int initialBalanceFirstCard = DashboardPage.getCardBalance(firstIdCard);
    int initialBalanceSecondCard = DashboardPage.getCardBalance(secondIdCard);
    int amountTransfer = -906;
    DashboardPage.transferBetweenOwnCards(firstIdCard, secondNumberCard, amountTransfer);
    int finalBalanceFirstCard = DashboardPage.getCardBalance(firstIdCard);
    int finalBalanceSecondCard = DashboardPage.getCardBalance(secondIdCard);
    //Проверка зачисления на первую карту, т.к. минус не введется:
    assertEquals(initialBalanceFirstCard - amountTransfer, finalBalanceFirstCard);
    //Проверка списания со второй карты, т.к. минус не введется:
    assertEquals(initialBalanceSecondCard + amountTransfer, finalBalanceSecondCard);
  }

  @Test   //Попытка перевода с первой карты на вторую с суммой перевода превышающей баланс первой карты
  @DisplayName("Transfer money from the first card to the second " +
          "with the transfer amount exceeding the balance of the first card")
  public void shouldTransferFromFirstToSecondNegativeAmount() {
    int initialBalanceFirstCard = DashboardPage.getCardBalance(firstIdCard);
    int initialBalanceSecondCard = DashboardPage.getCardBalance(secondIdCard);
    int amountTransfer = initialBalanceFirstCard + initialBalanceSecondCard + 1;
    DashboardPage.transferBetweenOwnCards(secondIdCard, firstNumberCard, amountTransfer);
    int finalBalanceFirstCard = DashboardPage.getCardBalance(firstIdCard);
    int finalBalanceSecondCard = DashboardPage.getCardBalance(secondIdCard);
    //Проверка зачисления на вторую карту суммы превышающей баланс первой карты:
    assertNotEquals(finalBalanceSecondCard, amountTransfer + initialBalanceSecondCard,
            "Осуществлен перевод превышающий баланс карты, с которой осуществляется перевод");
    //Проверка списания с первой карты суммы превышающей ее баланс:
    assertNotEquals(initialBalanceFirstCard, finalBalanceFirstCard,
            "Осуществлено списание суммы, при осуществлении перевода превышающего баланс карты");
  }

  @AfterEach
  public void cardBalancing() {
    int balanceFirstCard = DashboardPage.getCardBalance(firstIdCard);
    int balanceSecondCard = DashboardPage.getCardBalance(secondIdCard);
    int amountTransfer;
    if (balanceFirstCard > balanceSecondCard) {
      amountTransfer = (balanceFirstCard - balanceSecondCard) / 2;
      DashboardPage.transferBetweenOwnCards(secondIdCard, firstNumberCard, amountTransfer);
    }
    if (balanceFirstCard < balanceSecondCard) {
      amountTransfer = (balanceSecondCard - balanceFirstCard) / 2;
      DashboardPage.transferBetweenOwnCards(firstIdCard, secondNumberCard, amountTransfer);
    }
  }
}
```