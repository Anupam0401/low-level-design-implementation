package implement.lld.money;

public class Coin extends Money {
    public Coin(int number, Denomination denomination) {
        super(number, denomination, MoneyType.COIN);
    }
}
