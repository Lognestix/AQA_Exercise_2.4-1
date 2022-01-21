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