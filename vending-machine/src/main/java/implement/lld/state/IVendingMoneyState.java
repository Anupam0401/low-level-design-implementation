package implement.lld.state;

import implement.lld.product.Product;

public interface IVendingMoneyState {
    void selectProduct(Product product);
    void insertMoney(int quantity, String denomination);
    void dispenseProduct();
    void returnChange();
    void cancelTransaction();
    IVendingMoneyState getNextState();
}
