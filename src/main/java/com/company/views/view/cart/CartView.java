package com.company.views.view.cart;


import com.company.views.entity.Product;
import com.company.views.view.main.MainView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.util.UnknownOperationResult;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "cart-view", layout = MainView.class)
@ViewController("CartView")
@ViewDescriptor("cart-view.xml")
public class CartView extends StandardView {

    @ViewComponent
    private DataGrid<Product> productsDataGrid;
    @ViewComponent
    private Span cartTotalSpan;

    @ViewComponent
    private CollectionContainer<Product> productsDc;
    @ViewComponent
    private DataContext dataContext;

    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Dialogs dialogs;

    @Subscribe
    public void onInit(InitEvent event) {
        productsDataGrid.addComponentColumn(product -> {
            JmixButton addToCartButton = uiComponents.create(JmixButton.class);
            addToCartButton.setText("Add to cart");
            addToCartButton.addClickListener(clickEvent -> addToCart(product));
            return addToCartButton;
        });
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        updateTotal();
    }

    private void updateTotal() {
        double total = productsDc.getItems().stream()
                .mapToDouble(p -> p.getPrice() * p.getAmount())
                .sum();
        cartTotalSpan.setText(String.format("Total: $%.2f", total));
    }

    private void addToCart(Product product) {
        product.setAmount(product.getAmount() + 1);
        updateTotal();
    }

    @Subscribe
    public void onBeforeClose(BeforeCloseEvent event) {
//        dataContext.save();

        if (dataContext.hasChanges()) {
            CloseAction action = event.getCloseAction();
            if (action instanceof NavigateCloseAction navigateCloseAction) {
                BeforeLeaveEvent beforeLeaveEvent = navigateCloseAction.getBeforeLeaveEvent();
                beforeLeaveEvent.postpone();

                dialogs.createOptionDialog()
                        .withText("Save cart for later?")
                        .withActions(
                                new DialogAction(DialogAction.Type.YES)
                                        .withHandler(e -> {
                                            dataContext.save();
                                            close(StandardOutcome.SAVE);
                                        }),
                                new DialogAction(DialogAction.Type.NO)
                                        .withHandler(e -> close(StandardOutcome.DISCARD))
                        ).open();
            }
        }
    }
}
