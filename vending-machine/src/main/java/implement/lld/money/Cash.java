package implement.lld.money;

public class Cash extends Money {
    public Cash(int quantity, Denomination denomination) {
        super(quantity, denomination, MoneyType.CASH);
    }
}
