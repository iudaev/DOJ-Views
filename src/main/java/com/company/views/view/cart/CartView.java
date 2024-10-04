package com.company.views.view.cart;


import com.company.views.entity.Product;
import com.company.views.view.main.MainView;
import com.company.views.view.user.UserListView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "cart-view", layout = MainView.class)
@ViewController("CartView")
@ViewDescriptor("cart-view.xml")
public class CartView extends StandardView {

    @ViewComponent
    private DataGrid<Product> productsDataGrid;
    @ViewComponent
    private NativeLabel cartTotalLabel;
    @Autowired
    private UiComponents uiComponents;

    @ViewComponent
    private CollectionContainer<Product> productsDc;
    @ViewComponent
    private DataContext dataContext;
    @Autowired
    private Dialogs dialogs;


    @Subscribe
    public void onInit(InitEvent event) {
        productsDataGrid.addComponentColumn(product -> {
            JmixButton addToCartButton = uiComponents.create(JmixButton.class);
            addToCartButton.setText("Add to cart");
            addToCartButton.addClickListener(clickEvent -> {
                addToCart(product);
            });
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
        cartTotalLabel.setText(String.format("Total: $%.2f", total));
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
                                            close(StandardOutcome.SAVE);}),
                                new DialogAction(DialogAction.Type.NO)
                                        .withHandler(e -> close(StandardOutcome.CLOSE))
                        ).open();
            }
        }
    }

}
