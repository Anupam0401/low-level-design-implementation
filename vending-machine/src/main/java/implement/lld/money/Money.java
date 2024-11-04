package implement.lld.money;


public abstract class Money {
    protected int quantity;
    protected Denomination denomination;
    protected MoneyType moneyType;

    public Money(int quantity, Denomination denomination, MoneyType moneyType) {
        this.quantity = quantity;
        this.denomination = denomination;
        this.moneyType = moneyType;
    }

    public int getQuantity() {
        return quantity;
    }

    public Denomination getDenomination() {
        return denomination;
    }

    public double getMoneyValue() {
        return quantity * denomination.getDenominationValue();
    }

    public MoneyType getMoneyType() {
        return moneyType;
    }
}
