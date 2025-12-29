package com.stockapp.ui;

import com.stockapp.service.Storage;
import com.stockapp.model.Order;
import com.stockapp.model.User;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.util.Optional;

public class AccountManager {
    private static String currentUser = null;

    public static String getCurrentUser() { return currentUser; }

    public static boolean signUp(Window owner) {
        Dialog<Boolean> dlg = new Dialog<>();
        dlg.initOwner(owner);
        dlg.setTitle("Sign Up");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8); grid.setPadding(new Insets(10));
        TextField userF = new TextField(); userF.setPromptText("username");
        PasswordField pwF = new PasswordField(); pwF.setPromptText("password");
        TextField emailF = new TextField(); emailF.setPromptText("email (optional)");
        grid.add(new Label("Username:"), 0,0); grid.add(userF,1,0);
        grid.add(new Label("Password:"), 0,1); grid.add(pwF,1,1);
        grid.add(new Label("Email:"), 0,2); grid.add(emailF,1,2);
        dlg.getDialogPane().setContent(grid);
        dlg.setResultConverter(btn -> btn == ButtonType.OK);
        Optional<Boolean> res = dlg.showAndWait();
        if (res.isPresent() && res.get()) {
            String u = userF.getText().trim(); String p = pwF.getText(); String e = emailF.getText().trim();
            if (u.isEmpty() || p.isEmpty()) {
                showAlert("Username and password required");
                return false;
            }
            Storage dao = new Storage(); dao.initDB();
            boolean ok = dao.createUser(u, p, e);
            if (ok) {
                currentUser = u;
                return true;
            } else {
                showAlert("Sign up failed (username may already exist)");
                return false;
            }
        }
        return false;
    }

    public static boolean signIn(Window owner) {
        Dialog<Boolean> dlg = new Dialog<>();
        dlg.initOwner(owner);
        dlg.setTitle("Sign In");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane(); grid.setHgap(8); grid.setVgap(8); grid.setPadding(new Insets(10));
        TextField userF = new TextField(); userF.setPromptText("username");
        PasswordField pwF = new PasswordField(); pwF.setPromptText("password");
        grid.add(new Label("Username:"), 0,0); grid.add(userF,1,0);
        grid.add(new Label("Password:"), 0,1); grid.add(pwF,1,1);
        dlg.getDialogPane().setContent(grid);
        dlg.setResultConverter(btn -> btn == ButtonType.OK);
        Optional<Boolean> res = dlg.showAndWait();
        if (res.isPresent() && res.get()) {
            String u = userF.getText().trim(); String p = pwF.getText();
            if (u.isEmpty() || p.isEmpty()) { showAlert("Credentials required"); return false; }
            Storage dao = new Storage(); dao.initDB();
            boolean ok = dao.authenticate(u, p);
            if (ok) { currentUser = u; return true; }
            showAlert("Sign in failed: invalid credentials");
            return false;
        }
        return false;
    }

    public static Order placeOrder(Window owner, String defaultSymbol) {
        if (currentUser == null) { showAlert("Please sign in first"); return null; }
        Dialog<Order> dlg = new Dialog<>();
        dlg.initOwner(owner); dlg.setTitle("Place Order");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane(); grid.setHgap(8); grid.setVgap(8); grid.setPadding(new Insets(10));
        TextField symbolF = new TextField(defaultSymbol == null ? "" : defaultSymbol);
        ChoiceBox<String> sideBox = new ChoiceBox<>(); sideBox.getItems().addAll("BUY","SELL"); sideBox.setValue("BUY");
        TextField qtyF = new TextField(); qtyF.setPromptText("quantity");
        TextField priceF = new TextField(); priceF.setPromptText("price (0 = market)");
        grid.add(new Label("Symbol:"),0,0); grid.add(symbolF,1,0);
        grid.add(new Label("Side:"),0,1); grid.add(sideBox,1,1);
        grid.add(new Label("Quantity:"),0,2); grid.add(qtyF,1,2);
        grid.add(new Label("Price:"),0,3); grid.add(priceF,1,3);
        dlg.getDialogPane().setContent(grid);
        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String sym = symbolF.getText().trim(); String side = sideBox.getValue();
                    double qty = Double.parseDouble(qtyF.getText().trim());
                    double price = 0.0;
                    if (priceF.getText() != null && !priceF.getText().trim().isEmpty()) price = Double.parseDouble(priceF.getText().trim());
                    Order o = new Order(currentUser, sym, side, qty, price, "OPEN", LocalDateTime.now());
                    Storage dao = new Storage(); dao.initDB();
                    dao.insertOrder(o);
                    return o;
                } catch (Exception e) {
                    showAlert("Invalid order: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
        Optional<Order> res = dlg.showAndWait();
        return res.orElse(null);
    }

    private static void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }
}
