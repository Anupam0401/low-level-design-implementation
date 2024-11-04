package implement.lld;

import implement.lld.product.Inventory;
import implement.lld.product.InventoryManager;
import implement.lld.product.Product;
import implement.lld.state.DispenseVendingMoneyState;
import implement.lld.state.IVendingMoneyState;
import implement.lld.state.IdleVendingMoneyState;
import implement.lld.state.ReadyVendingMoneyState;
import implement.lld.state.ReturnMoneyVendingMoneyState;

public class VendingMachine {
    private static VendingMachine instance;
    private IVendingMoneyState vendingMoneyState;
    private Product selectedProduct;
    private double currentPayment;
    private final InventoryManager inventoryManager;

    private final IdleVendingMoneyState idleVendingMoneyState;
    private final ReadyVendingMoneyState readyVendingMoneyState;

    private final ReturnMoneyVendingMoneyState returnMoneyVendingMoneyState;

    private final DispenseVendingMoneyState dispenseVendingMoneyState;
    public double getCurrentPayment() {
        return currentPayment;
    }

    public Product getSelectedProduct() {
        return selectedProduct;
    }

    public IVendingMoneyState getVendingMoneyState() {
        return vendingMoneyState;
    }

    private VendingMachine() {
        Inventory inventory = new Inventory();
        this.idleVendingMoneyState = new IdleVendingMoneyState(this);
        this.readyVendingMoneyState = new ReadyVendingMoneyState(this);
        this.returnMoneyVendingMoneyState = new ReturnMoneyVendingMoneyState(this);
        this.dispenseVendingMoneyState = new DispenseVendingMoneyState(this);
        this.vendingMoneyState = idleVendingMoneyState;
        this.inventoryManager = new InventoryManager(inventory);
        this.selectedProduct = null;
        this.currentPayment = 0.0;
    }

    public static synchronized VendingMachine getInstance() {
        if (instance == null) {
            instance = new VendingMachine();
        }
        return instance;
    }

    public IdleVendingMoneyState getIdleVendingMoneyState() {
        return idleVendingMoneyState;
    }

    public ReadyVendingMoneyState getReadyVendingMoneyState() {
        return readyVendingMoneyState;
    }

    public ReturnMoneyVendingMoneyState getReturnMoneyVendingMoneyState() {
        return returnMoneyVendingMoneyState;
    }

    public DispenseVendingMoneyState getDispenseVendingMoneyState() {
        return dispenseVendingMoneyState;
    }

    public void setSelectedProduct(Product selectedProduct) {
        this.selectedProduct = selectedProduct;
    }

    public void setVendingMoneyState(IVendingMoneyState vendingMoneyState) {
        this.vendingMoneyState = vendingMoneyState;
    }

    public void setCurrentPayment(double currentPayment) {
        this.currentPayment = currentPayment;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public void transitionToNextState() {
        this.vendingMoneyState = vendingMoneyState.getNextState();
    }

    public void resetPayment() {
        this.currentPayment = 0.0;
    }

    public void resetSelectedProduct() {
        this.selectedProduct = null;
    }
}
