package Model;

import Utils.DBController;
import Utils.DBQuery;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.ListIterator;

public class Customer
{
    private int customerId;
    private int addressId;
    private String customerName;
    private String address;
    private String address2;
    private String country;
    private String city;
    private String postalCode;
    private String phone;
    private int active;
    private Date createDate;
    private String createdBy;

    public Customer()
    {

    }

    public Customer(String customerName, String address, String address2, String country, String city, String postalCode, String phone)
    {
        this.customerName = customerName;
        this.address = address;
        this.address2 = address2;
        this.country = country;
        this.city = city;
        this.postalCode = postalCode;
        this.phone = phone;
    }

    public static ObservableList<Customer> customerSearch(String search) throws SQLException
    {
        ObservableList<Customer> allCustomers = DBController.getAllCustomers();
        ListIterator<Customer> customerIterator = allCustomers.listIterator();
        ObservableList<Customer> searchResult = FXCollections.observableArrayList();
        while(customerIterator.hasNext())
        {
            Customer currCustomer = customerIterator.next();
            if(currCustomer.getCustomerName().contains(search))
            {
                searchResult.add(currCustomer);
            }
        }

        if (searchResult.size() == 0)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Search Result");
            alert.setHeaderText("No Matches Found");
            alert.setContentText("Your search did not have any matches.");
            alert.showAndWait();
        }
        return searchResult;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAddress() { return address; }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getActive() { return active; }

    public void setActive(int active) { this.active = active; }
}
