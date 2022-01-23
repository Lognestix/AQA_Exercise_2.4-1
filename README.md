## `Статус сборки` [![Build status](https://ci.appveyor.com/api/projects/status/9ypd2a315qyhsrjq?svg=true)](https://ci.appveyor.com/project/Lognestix/aqa-exercise-2-4-1)
## В build.gradle добавленна поддержка JUnit-Jupiter, Selenide и headless-режим, Simple, Lombok.
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
    testImplementation 'org.slf4j:slf4j-simple:1.7.33'

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

import com.codeborne.selenide.SelenideElement;
import ru.netology.data.DataHelper;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class DashboardPage {
  private final SelenideElement heading = $("[data-test-id=dashboard]");
  private final SelenideElement title = $("h1.heading");
  private final String balanceStart = "баланс:";
  private final String balanceFinish = "р.";

  public DashboardPage() {
    heading.shouldBe(visible);
    title.shouldHave(text("Ваши карты"));
  }

  private int extractBalance(String cardInfo) {
    //Вырезается нужная часть строки:
    var value = cardInfo.substring
            //Начальная позиция (исключительно) плюс смещение:
            (cardInfo.indexOf(balanceStart) + balanceStart.length(),
                    //Конечная позиция (включительно):
                    cardInfo.indexOf(balanceFinish))
            //Обрезка начального и конечного пробелов:
            .trim();
    return Integer.parseInt(value);
  }

  public int getCardBalance(DataHelper.CardId cardId) {
    return extractBalance($("[data-test-id='" + cardId.getId() + "']").getText());
  }

  public ReplenishmentPage transfer(DataHelper.CardId cardId) {
    $("[data-test-id='" + cardId.getId() + "'] [data-test-id=action-deposit]").click();
    return new ReplenishmentPage();
  }
}
```
```Java
package ru.netology.page;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Keys;
import ru.netology.data.DataHelper;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class ReplenishmentPage {
    private final SelenideElement heading = $("[data-test-id=dashboard]");
    private final SelenideElement title = $("h1.heading");
    private final SelenideElement amountField = $("[data-test-id=amount] input");
    private final SelenideElement fromField = $("[data-test-id=from] input");
    private final SelenideElement transferButton = $("[data-test-id=action-transfer]");
    private final SelenideElement errorNotification = $("[data-test-id=error-notification]");
    private final SelenideElement cancelButton = $("[data-test-id=action-cancel]");

    public ReplenishmentPage() {
        heading.shouldBe(visible);
        title.shouldHave(text("Пополнение карты"));
    }

    private void fieldClearing() {
        amountField.sendKeys(Keys.CONTROL + "a");
        amountField.sendKeys(Keys.DELETE);
        fromField.sendKeys(Keys.CONTROL + "a");
        fromField.sendKeys(Keys.DELETE);
    }

    public DashboardPage transferBetweenOwnCards(DataHelper.TransferInfo transferInfo) {
        fieldClearing();
        amountField.setValue(String.valueOf(transferInfo.getAmount()));
        fromField.setValue(transferInfo.getNumberCard());
        transferButton.click();
        if(errorNotification.is(visible)) {
            cancelButton.click();
        }
        return new DashboardPage();
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

  @BeforeAll
  public static void loginToPersonalAccount() {
    //Вместо первых двух команд можно использовать эту:
    //var loginPage = open("http://localhost:9999", LoginPage.class);
    open("http://localhost:9999");
    var loginPage = new LoginPage();
    var authInfo = DataHelper.getUserAuthInfo();
    var verificationPage = loginPage.validLogin(authInfo);
    var verificationCode = DataHelper.getVerificationCodeFor();
    verificationPage.validVerify(verificationCode);
  }

  @AfterEach
  public void cardBalancing() {
    //Получение баланса по обеим картам:
    var dashboardPage = new DashboardPage();
    var firstCardId = DataHelper.getFirstCardId();
    var balanceFirstCard = dashboardPage.getCardBalance(firstCardId);
    var secondCardId = DataHelper.getSecondCardId();
    var balanceSecondCard = dashboardPage.getCardBalance(secondCardId);
    //Определение на какую карту и сколко переводить для выравнивания баланса:
    int amountTransfer;
    if (balanceFirstCard > balanceSecondCard) {
      amountTransfer = (balanceFirstCard - balanceSecondCard) / 2;
      var replenishmentPage = dashboardPage.transfer(secondCardId);
      var transferInfo = DataHelper.setSecondCardTransferInfo(amountTransfer);
      //Осуществление перевода денег:
      replenishmentPage.transferBetweenOwnCards(transferInfo);
    }
    if (balanceFirstCard < balanceSecondCard) {
      amountTransfer = (balanceSecondCard - balanceFirstCard) / 2;
      var replenishmentPage = dashboardPage.transfer(firstCardId);
      var transferInfo = DataHelper.setFirstCardTransferInfo(amountTransfer);
      //Осуществление перевода денег:
      replenishmentPage.transferBetweenOwnCards(transferInfo);
    }
  }

          //Позитивные проверки:
  @Test   //Перевод со второй карты на первую
  @DisplayName("Transfer money from the second card to the first card")
  public void shouldTransferFromSecondToFirst() {
    //Получение баланса по обеим картам и подготовка данных для перевода денег:
    var dashboardPage = new DashboardPage();
    var firstCardId = DataHelper.getFirstCardId();
    var initialBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
    var secondCardId = DataHelper.getSecondCardId();
    var initialBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
    var replenishmentPage = dashboardPage.transfer(firstCardId);
    var transferInfo = DataHelper.getFirstCardTransferInfoPositive();
    //Осуществление перевода денег:
    replenishmentPage.transferBetweenOwnCards(transferInfo);
    //Получение итогового баланса по обеим картам:
    var finalBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
    var finalBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
    //Проверка зачисления на первую карту:
    assertEquals(transferInfo.getAmount(), finalBalanceFirstCard - initialBalanceFirstCard);
    //Проверка списания со второй карты:
    assertEquals(transferInfo.getAmount(), initialBalanceSecondCard - finalBalanceSecondCard);
  }

  @Test   //Перевод с первой карты на вторую
  @DisplayName("Transfer money from the first card to the second card")
  public void shouldTransferFromFirstToSecond() {
    var dashboardPage = new DashboardPage();
    var firstCardId = DataHelper.getFirstCardId();
    var initialBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
    var secondCardId = DataHelper.getSecondCardId();
    var initialBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
    var replenishmentPage = dashboardPage.transfer(secondCardId);
    var transferInfo = DataHelper.getSecondCardTransferInfoPositive();
    //Осуществление перевода денег:
    replenishmentPage.transferBetweenOwnCards(transferInfo);
    //Получение итогового баланса по обеим картам:
    var finalBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
    var finalBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
    //Проверка списания с первой карты:
    assertEquals(transferInfo.getAmount(), initialBalanceFirstCard - finalBalanceFirstCard);
    //Проверка зачисления на вторую карту:
    assertEquals(transferInfo.getAmount(), finalBalanceSecondCard - initialBalanceSecondCard);
  }

          //Негативные проверки:
  @Test   //Попытка перевода со второй карты на первую с отрицательной суммой перевода
  @DisplayName("Transferring money from the second card to the first card with a negative amount")
  public void shouldTransferFromSecondToFirstNegativeAmount() {
    //Получение баланса по обеим картам и подготовка данных для перевода денег:
    var dashboardPage = new DashboardPage();
    var firstCardId = DataHelper.getFirstCardId();
    var initialBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
    var secondCardId = DataHelper.getSecondCardId();
    var initialBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
    var replenishmentPage = dashboardPage.transfer(firstCardId);
    var transferInfo = DataHelper.getFirstCardTransferInfoNegative();
    //Т.к. минус при вводе суммы игнорируется будет обычный перевод
    //Осуществление перевода денег:
    replenishmentPage.transferBetweenOwnCards(transferInfo);
    //Получение итогового баланса по обеим картам:
    var finalBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
    var finalBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
    //Проверка зачисления на первую карту:
    assertEquals( - transferInfo.getAmount(), finalBalanceFirstCard - initialBalanceFirstCard);
    //Проверка списания со второй карты:
    assertEquals( - transferInfo.getAmount(), initialBalanceSecondCard - finalBalanceSecondCard);
  }

  @Test   //Попытка перевода с первой карты на вторую с суммой перевода превышающей баланс первой карты
  @DisplayName("Transfer money from the first card to the second " +
          "with the transfer amount exceeding the balance of the first card")
  public void shouldTransferFromFirstToSecondNegativeAmount() {
    //Получение баланса по обеим картам и подготовка данных для перевода денег:
    var dashboardPage = new DashboardPage();
    var firstCardId = DataHelper.getFirstCardId();
    var initialBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
    var secondCardId = DataHelper.getSecondCardId();
    var initialBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
    var replenishmentPage = dashboardPage.transfer(secondCardId);
    var transferInfo = DataHelper.getSecondCardTransferInfoNegative();
    //Попытка осуществление перевода денег:
    replenishmentPage.transferBetweenOwnCards(transferInfo);
    //Получение итогового баланса по обеим картам:
    var finalBalanceFirstCard = dashboardPage.getCardBalance(firstCardId);
    var finalBalanceSecondCard = dashboardPage.getCardBalance(secondCardId);
    //Проверка на изменение баланса первой карты:
    assertEquals(initialBalanceFirstCard, finalBalanceFirstCard,
            "Изменился баланс первой карты");
    //Проверка на изменение баланса второй карты:
    assertEquals(initialBalanceSecondCard, finalBalanceSecondCard,
            "Изменился баланс второй карты");
  }
}
```