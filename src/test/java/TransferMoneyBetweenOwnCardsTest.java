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