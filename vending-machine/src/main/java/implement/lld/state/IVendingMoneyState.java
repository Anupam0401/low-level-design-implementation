package implement.lld.state;

import implement.lld.money.Denomination;

public interface IVendingMoneyState {
    void selectProduct(String productName);
    void insertMoney(int quantity, Denomination denomination);
    void dispenseProduct();
    void returnChange();
    void cancelTransaction();
    IVendingMoneyState getNextState();
}
