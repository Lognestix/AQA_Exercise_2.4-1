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