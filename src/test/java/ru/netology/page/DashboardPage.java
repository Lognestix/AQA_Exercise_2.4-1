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