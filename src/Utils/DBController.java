package Utils;

import Model.Appointment;
import Model.Customer;
import Model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.CustomerListController;
import main.LoginScreenController;
import main.MainScreenController;

import java.sql.*;
import java.time.*;
import java.util.ListIterator;
import java.util.Set;
import java.util.TimeZone;

public class DBController
{
    public static ObservableList<Customer> allCustomers = FXCollections.observableArrayList();
    static String currentUser = LoginScreenController.getCurrentUser();
    public static ZoneId defaultZoneId = ZoneId.of(TimeZone.getDefault().getID());
    public static ZoneId zoneUTC = ZoneId.of("UTC");

    public static void addCustomer(Customer customer) throws SQLException
    {
        Integer countryId = addCountry(customer.getCountry());
        Integer cityId = addCity(customer.getCity(), countryId);
        Integer addressId = addAddress(customer.getAddress(), customer.getAddress2(), cityId, customer.getPostalCode(), customer.getPhone());

        String customerName = customer.getCustomerName();

        Statement statement = DBQuery.getStatement();
        String addCustomer = "INSERT INTO customer (customerName, addressId, active, createDate, createdBy, lastUpdate, lastUpdateBy) " +
                "VALUES ('" + customerName + "', " + addressId + ", 1, NOW(), '" + currentUser + "', NOW(), '" + currentUser + "');";
        statement.execute(addCustomer);
    }

    public static void updateCustomer(Customer customer) throws SQLException
    {
        Customer originalCustomer = null;
        if (CustomerListController.customerToEdit != null)
        {
            originalCustomer = CustomerListController.customerToEdit;
        }
        else if (MainScreenController.customerToEdit != null)
        {
            originalCustomer = MainScreenController.customerToEdit;
        }
        Statement statement = DBQuery.getStatement();
        Integer addressId = null;
        boolean countryChange = false, cityChange = false, addressChange = false;

        if (!(customer.getCountry().equals(originalCustomer.getCountry())))
        {
            countryChange = true;
            Integer countryId = addCountry(customer.getCountry());
            Integer cityId = addCity(customer.getCity(), countryId);
            addressId = addAddress(customer.getAddress(), customer.getAddress2(), cityId, customer.getPostalCode(), customer.getPhone());
        }

        else if (!(customer.getCity().equals(originalCustomer.getCity())))
        {
            cityChange = true;
            Integer cityId = addCity(customer.getCity(), getCountryId(customer.getCountry()));
            addressId = addAddress(customer.getAddress(), customer.getAddress2(), cityId, customer.getPostalCode(), customer.getPhone());
        }

        else if (!(customer.getAddress().equals(originalCustomer.getAddress())) || !(customer.getAddress2().equals(originalCustomer.getAddress2()))
                || !(customer.getPostalCode().equals(originalCustomer.getPostalCode())) || (customer.getPhone().equals(originalCustomer.getPhone())))
        {
            addressChange = true;
            addressId = addAddress(customer.getAddress(), customer.getAddress2(), getCityId(customer.getCity()), customer.getPostalCode(), customer.getPhone());
        }

        if ((countryChange) || (cityChange) || (addressChange))
        {
            String query = "UPDATE customer SET customerName = '" + customer.getCustomerName() +
                    "', addressId = '" + addressId + "', lastUpdate = NOW(), lastUpdateBy = '" + currentUser + "' " +
                    "WHERE customerId = " + originalCustomer.getCustomerId() + ";";
            statement.execute(query);
        }
        else
        {
            String updateCustomer = "UPDATE customer SET customerName = '" + customer.getCustomerName() + "' WHERE customerId = " + originalCustomer.getCustomerId() + ";";
            statement.execute(updateCustomer);
        }
    }

    public static void deleteCustomer(Integer id) throws SQLException
    {
        Statement statement = DBQuery.getStatement();
        String deleteAppt = "DELETE FROM appointment WHERE customerId = " + id + ";";
        statement.execute(deleteAppt);
        String deleteCustomer = "DELETE FROM customer WHERE customerId = " + id + ";";
        statement.execute(deleteCustomer);
    }

    public static Customer getCustomerDetails(Integer customerId) throws SQLException
    {
        Customer newCustomer = new Customer();
        Integer addressId = null;
        Integer cityId = null;
        Integer countryId = null;
        Statement statement = DBQuery.getStatement();
        String addressIdQuery = "SELECT * FROM customer WHERE customerId = " + customerId + ";";
        statement.execute(addressIdQuery);
        ResultSet addressRS = statement.getResultSet();
        while (addressRS.next())
        {
            newCustomer.setCustomerId(addressRS.getInt("customerId"));
            newCustomer.setCustomerName(addressRS.getString("customerName"));
            addressId = addressRS.getInt("addressId");
        }

        String cityIdQuery = "SELECT * FROM address WHERE addressId = " + addressId + ";";
        statement.execute(cityIdQuery);
        ResultSet cityRS = statement.getResultSet();
        while (cityRS.next())
        {
            cityId = cityRS.getInt("cityId");
            newCustomer.setAddressId(cityRS.getInt("addressId"));
            newCustomer.setAddress(cityRS.getString("address"));
            newCustomer.setAddress2(cityRS.getString("address2"));
            newCustomer.setPostalCode(cityRS.getString("postalCode"));
            newCustomer.setPhone(cityRS.getString("phone"));
        }

        String countryIdQuery = "SELECT * FROM city WHERE cityId = " + cityId + ";";
        statement.execute(countryIdQuery);
        ResultSet countryRS = statement.getResultSet();
        while (countryRS.next())
        {
            countryId = countryRS.getInt("countryId");
            newCustomer.setCity(countryRS.getString("city"));
        }

        String countryQuery = "SELECT * FROM country WHERE countryId = " + countryId + ";";
        statement.execute(countryQuery);
        ResultSet rs = statement.getResultSet();
        while (rs.next())
        {
            newCustomer.setCountry(rs.getString("country"));
        }
        return newCustomer;
    }

    public static ObservableList<Customer> getAllCustomers() throws SQLException
    {
        ObservableList<Customer> customers = FXCollections.observableArrayList();
        ObservableList<Customer> detailedCustomers = FXCollections.observableArrayList();
        Statement statement = DBQuery.getStatement();
        String customersQuery = "SELECT * FROM customer;";
        statement.execute(customersQuery);
        ResultSet customersRs = statement.getResultSet();

        while (customersRs.next())
        {
            Customer customer = new Customer();
            //Customer detailedCustomer = DBController.getCustomerDetails(customersRs.getInt("customerId")); git test
            //Customer customer = DBController.getCustomerDetails(customersRs.getInt("customerId"));


            customer.setCustomerId(customersRs.getInt("customerId"));
            customer.setCustomerName(customersRs.getString("customerName"));
            customer.setActive(customersRs.getInt("active"));
            customer.setCreateDate(customersRs.getDate("createDate"));
            customer.setCreatedBy(customersRs.getString("createdBy"));
            customers.add(customer);
        }

        ListIterator<Customer> customerList = customers.listIterator();
        while (customerList.hasNext())
        {
            Customer detailedCustomer = DBController.getCustomerDetails(customerList.next().getCustomerId());
            detailedCustomers.add(detailedCustomer);
        }
        return detailedCustomers;
    }

    public static Integer addCountry(String country) throws SQLException
    {
        Integer countryId = null;
        Statement statement = DBQuery.getStatement();
        String addCountry = "INSERT INTO country (country, createDate, createdBy, lastUpdate, lastUpdateBy) " +
                "VALUES ('" + country + "', NOW(), '" + currentUser + "', NOW(), '" + currentUser + "');";
        statement.execute(addCountry);

        String countryQuery = "SELECT countryId FROM country WHERE country = '" + country + "';";
        statement.execute(countryQuery);
        ResultSet countryRS = statement.getResultSet();
        while (countryRS.next())
        {
            countryId = countryRS.getInt("countryId");
        }
        return countryId;
    }

    public static Integer getCountryId(String country) throws SQLException
    {
        Integer countryId = null;
        Statement statement = DBQuery.getStatement();
        String findCountry = "SELECT * FROM country WHERE country = '" + country + "';";
        statement.execute(findCountry);
        ResultSet rs = statement.getResultSet();
        while (rs.next())
        {
            countryId = rs.getInt("countryId");
        }
        return countryId;
    }

    public static Integer addCity(String city, Integer countryId) throws SQLException
    {
        Integer cityId = null;
        Statement statement = DBQuery.getStatement();
        String addCity = "INSERT INTO city (city, countryId, createDate, createdBy, lastUpdate, lastUpdateBy) " +
                "VALUES ('" + city + "', " + countryId + ", NOW(), '" + currentUser + "', NOW(), '" + currentUser + "');";
        statement.execute(addCity);

        String cityQuery = "SELECT cityId FROM city WHERE city = '" + city + "';";
        statement.execute(cityQuery);
        ResultSet cityRS = statement.getResultSet();
        while (cityRS.next())
        {
            cityId = cityRS.getInt("cityId");
        }
        return cityId;
    }

    public static Integer getCityId(String city) throws SQLException
    {
        Integer cityId = null;
        Statement statement = DBQuery.getStatement();
        String findCity = "SELECT * FROM city WHERE city = '" + city + "';";
        statement.execute(findCity);
        ResultSet rs = statement.getResultSet();
        while (rs.next())
        {
            cityId = rs.getInt("cityId");
        }
        return cityId;
    }

    public static Integer addAddress(String address, String address2, Integer cityId, String postalCode, String phone) throws SQLException
    {
        Integer addressId = null;
        Statement statement = DBQuery.getStatement();
        String addAddress = "INSERT INTO address (address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdate, lastUpdateBy) " +
                "VALUES ('" + address + "', '" + address2 + "', " + cityId + ", '" + postalCode + "', '" + phone + "', NOW(), '" + currentUser + "', NOW(), '" + currentUser + "');";
        statement.execute(addAddress);

        String addressQuery = "SELECT addressId FROM address WHERE address = '" + address + "';";
        statement.execute(addressQuery);
        ResultSet addressRS = statement.getResultSet();
        while (addressRS.next())
        {
            addressId = addressRS.getInt("addressId");
        }
        return addressId;
    }

    public static ObservableList<User> getAllUsers() throws SQLException
    {
        ObservableList<User> allUsers = FXCollections.observableArrayList();
        Statement statement = DBQuery.getStatement();
        String query = "SELECT * FROM user;";
        statement.execute(query);
        ResultSet rs = statement.getResultSet();
        while (rs.next())
        {
            User user = new User();
            user.setUserId(rs.getInt("userId"));
            user.setUserName(rs.getString("userName"));
            allUsers.add(user);
        }
        return allUsers;
    }

    public static Integer getUserId(String user) throws SQLException
    {
        Statement statement = DBQuery.getStatement();
        Integer userId = null;
        String query = "SELECT * FROM user WHERE userName = '" + user + "';";
        statement.execute(query);
        ResultSet rs = statement.getResultSet();
        while (rs.next())
        {
            userId = rs.getInt("userId");
        }
        return userId;
    }

    public static void addAppointment(Customer customer, String title, String location, String type, ZonedDateTime start, ZonedDateTime end) throws SQLException
    {
        Statement statement = DBQuery.getStatement();
        Integer customerId = customer.getCustomerId();
        Integer userId = LoginScreenController.currentUser.getUserId();
        ZonedDateTime startUTC = start.withZoneSameInstant(zoneUTC);
        LocalDateTime startDatetime = startUTC.toLocalDateTime();
        ZonedDateTime endUTC = end.withZoneSameInstant(zoneUTC);
        LocalDateTime endDatetime = endUTC.toLocalDateTime();
        String addApptQuery = "INSERT INTO appointment (customerId, userId, title, description, location, contact, type, url, start, end, createDate, createdBy, lastUpdateBy) " +
                "VALUES (" + customerId + ", " + userId + ", '" + title + "', 'Description', '" + location + "', 'Contact', '" + type + "', 'url', '" + startDatetime + "', '" + endDatetime + "', NOW(), '" + currentUser + "', '" + currentUser + "');";
        statement.execute(addApptQuery);
    }

    public static void updateAppointment(Integer appointmentId, String title, String location, String type, ZonedDateTime start, ZonedDateTime end) throws SQLException
    {
        Statement statement = DBQuery.getStatement();
        ZonedDateTime startUTC = start.withZoneSameInstant(zoneUTC);
        LocalDateTime startDatetime = startUTC.toLocalDateTime();
        ZonedDateTime endUTC = end.withZoneSameInstant(zoneUTC);
        LocalDateTime endDatetime = endUTC.toLocalDateTime();
        String updateApptQuery = "UPDATE appointment SET title = '" + title + "', location = '" + location + "', type = '" +
                type + "', start = '" + startDatetime + "', end = '" + endDatetime + "' WHERE appointmentId = " + appointmentId + ";";
        statement.execute(updateApptQuery);
    }

    public static void deleteAppointment(Integer appointmentId) throws SQLException
    {
        Statement statement = DBQuery.getStatement();
        String query = "DELETE FROM appointment WHERE appointmentId = " + appointmentId + ";";
        statement.execute(query);
    }

    public static ObservableList<Appointment> getPastAppointments() throws SQLException
    {
        ObservableList<Appointment> allAppointments = FXCollections.observableArrayList();
        Statement statement = DBQuery.getStatement();
        String apptQuery = "SELECT * FROM appointment WHERE end < NOW();";
        statement.execute(apptQuery);
        ResultSet apptRS = statement.getResultSet();
        while (apptRS.next())
        {
            Appointment appt = new Appointment();

            LocalDate startDate = apptRS.getDate("start").toLocalDate();
            LocalTime startTime = apptRS.getTime("start").toLocalTime();
            LocalDateTime start = startTime.atDate(startDate);
            ZonedDateTime startDatetimeUTC = start.atZone(zoneUTC);
            ZonedDateTime startDatetimeZoned = startDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalDate localStartDate = startDatetimeZoned.toLocalDate();
            LocalTime localStartTime = startDatetimeZoned.toLocalTime();
            LocalDate endDate = apptRS.getDate("end").toLocalDate();
            LocalTime endTime = apptRS.getTime("end").toLocalTime();
            LocalDateTime end = endTime.atDate(endDate);
            ZonedDateTime endDatetimeUTC = end.atZone(zoneUTC);
            ZonedDateTime endDatetimeZoned = endDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalTime localEndTime = endDatetimeZoned.toLocalTime();

            appt.setAppointmentId(apptRS.getInt("appointmentId"));
            appt.setCustomerId(apptRS.getInt("customerId"));
            appt.setTitle(apptRS.getString("title"));
            appt.setLocation(apptRS.getString("location"));
            appt.setType(apptRS.getString("type"));
            appt.setDate(localStartDate);
            appt.setStartTime(localStartTime);
            appt.setEndTime(localEndTime);

            allAppointments.add(appt);
        }

        return allAppointments;
    }

    public static ObservableList<Appointment> getAllAppointments() throws SQLException
    {
        ObservableList<Appointment> allAppointments = FXCollections.observableArrayList();
        Statement statement = DBQuery.getStatement();
        String apptQuery = "SELECT * FROM appointment;";
        statement.execute(apptQuery);
        ResultSet apptRS = statement.getResultSet();
        while (apptRS.next())
        {
            Appointment appt = new Appointment();

            LocalDate startDate = apptRS.getDate("start").toLocalDate();
            LocalTime startTime = apptRS.getTime("start").toLocalTime();
            LocalDateTime start = startTime.atDate(startDate);
            ZonedDateTime startDatetimeUTC = start.atZone(zoneUTC);
            ZonedDateTime startDatetimeZoned = startDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalDate localStartDate = startDatetimeZoned.toLocalDate();
            LocalTime localStartTime = startDatetimeZoned.toLocalTime();
            LocalDate endDate = apptRS.getDate("end").toLocalDate();
            LocalTime endTime = apptRS.getTime("end").toLocalTime();
            LocalDateTime end = endTime.atDate(endDate);
            ZonedDateTime endDatetimeUTC = end.atZone(zoneUTC);
            ZonedDateTime endDatetimeZoned = endDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalTime localEndTime = endDatetimeZoned.toLocalTime();

            appt.setAppointmentId(apptRS.getInt("appointmentId"));
            appt.setCustomerId(apptRS.getInt("customerId"));
            appt.setTitle(apptRS.getString("title"));
            appt.setLocation(apptRS.getString("location"));
            appt.setType(apptRS.getString("type"));
            appt.setDate(localStartDate);
            appt.setStartTime(localStartTime);
            appt.setEndTime(localEndTime);

            allAppointments.add(appt);
        }

        return allAppointments;
    }

    public static ObservableList<Appointment> getUpcomingAppointments() throws SQLException
    {
        ObservableList<Appointment> allAppointments = FXCollections.observableArrayList();
        Statement statement = DBQuery.getStatement();
        String apptQuery = "SELECT * FROM appointment WHERE start >= NOW();";
        statement.execute(apptQuery);
        ResultSet apptRS = statement.getResultSet();
        while (apptRS.next())
        {
            Appointment appt = new Appointment();

            LocalDate startDate = apptRS.getDate("start").toLocalDate();
            LocalTime startTime = apptRS.getTime("start").toLocalTime();
            LocalDateTime start = startTime.atDate(startDate);
            ZonedDateTime startDatetimeUTC = start.atZone(zoneUTC);
            ZonedDateTime startDatetimeZoned = startDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalDate localStartDate = startDatetimeZoned.toLocalDate();
            LocalTime localStartTime = startDatetimeZoned.toLocalTime();
            LocalDate endDate = apptRS.getDate("end").toLocalDate();
            LocalTime endTime = apptRS.getTime("end").toLocalTime();
            LocalDateTime end = endTime.atDate(endDate);
            ZonedDateTime endDatetimeUTC = end.atZone(zoneUTC);
            ZonedDateTime endDatetimeZoned = endDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalTime localEndTime = endDatetimeZoned.toLocalTime();

            appt.setAppointmentId(apptRS.getInt("appointmentId"));
            appt.setCustomerId(apptRS.getInt("customerId"));
            appt.setTitle(apptRS.getString("title"));
            appt.setLocation(apptRS.getString("location"));
            appt.setType(apptRS.getString("type"));
            appt.setDate(localStartDate);
            appt.setStartTime(localStartTime);
            appt.setEndTime(localEndTime);

            allAppointments.add(appt);
        }

        return allAppointments;
    }

    public static ObservableList<Appointment> getAppointmentsWeekly(LocalDate startWeek, LocalDate endWeek) throws SQLException
    {
        ObservableList<Appointment> appointmentsWeekly = FXCollections.observableArrayList();
        Statement statement = DBQuery.getStatement();
        Date startWeekDate = Date.valueOf(startWeek);
        Date endWeekDate = Date.valueOf(endWeek);
        String weeklyQuery = "SELECT * FROM appointment WHERE start >= '" + startWeekDate + "' AND start <= '" + endWeekDate + "';";
        statement.execute(weeklyQuery);
        ResultSet weeklyRS = statement.getResultSet();
        while (weeklyRS.next())
        {
            Appointment appt = new Appointment();

            LocalDate startDate = weeklyRS.getDate("start").toLocalDate();
            LocalTime startTime = weeklyRS.getTime("start").toLocalTime();
            LocalDateTime start = startTime.atDate(startDate);
            ZonedDateTime startDatetimeUTC = start.atZone(zoneUTC);
            ZonedDateTime startDatetimeZoned = startDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalDate localStartDate = startDatetimeZoned.toLocalDate();
            LocalTime localStartTime = startDatetimeZoned.toLocalTime();
            LocalDate endDate = weeklyRS.getDate("end").toLocalDate();
            LocalTime endTime = weeklyRS.getTime("end").toLocalTime();
            LocalDateTime end = endTime.atDate(endDate);
            ZonedDateTime endDatetimeUTC = end.atZone(zoneUTC);
            ZonedDateTime endDatetimeZoned = endDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalTime localEndTime = endDatetimeZoned.toLocalTime();

            appt.setAppointmentId(weeklyRS.getInt("appointmentId"));
            appt.setCustomerId(weeklyRS.getInt("customerId"));
            appt.setTitle(weeklyRS.getString("title"));
            appt.setLocation(weeklyRS.getString("location"));
            appt.setType(weeklyRS.getString("type"));
            appt.setDate(localStartDate);
            appt.setStartTime(localStartTime);
            appt.setEndTime(localEndTime);

            appointmentsWeekly.add(appt);
        }

        return appointmentsWeekly;
    }

    public static ObservableList<Appointment> getAppointmentsMonthly(LocalDate startMonth, LocalDate endMonth) throws SQLException
    {
        ObservableList<Appointment> appointmentsMonthly = FXCollections.observableArrayList();
        Statement statement = DBQuery.getStatement();
        Date startMonthDate = Date.valueOf(startMonth);
        Date endMonthDate = Date.valueOf(endMonth);
        String monthlyQuery = "SELECT * FROM appointment WHERE start >= '" + startMonthDate + "' AND start <= '" + endMonthDate + "';";
        statement.execute(monthlyQuery);
        ResultSet monthlyRS = statement.getResultSet();
        while (monthlyRS.next())
        {
            Appointment appt = new Appointment();

            LocalDate startDate = monthlyRS.getDate("start").toLocalDate();
            LocalTime startTime = monthlyRS.getTime("start").toLocalTime();
            LocalDateTime start = startTime.atDate(startDate);
            ZonedDateTime startDatetimeUTC = start.atZone(zoneUTC);
            ZonedDateTime startDatetimeZoned = startDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalDate localStartDate = startDatetimeZoned.toLocalDate();
            LocalTime localStartTime = startDatetimeZoned.toLocalTime();
            LocalDate endDate = monthlyRS.getDate("end").toLocalDate();
            LocalTime endTime = monthlyRS.getTime("end").toLocalTime();
            LocalDateTime end = endTime.atDate(endDate);
            ZonedDateTime endDatetimeUTC = end.atZone(zoneUTC);
            ZonedDateTime endDatetimeZoned = endDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalTime localEndTime = endDatetimeZoned.toLocalTime();

            appt.setAppointmentId(monthlyRS.getInt("appointmentId"));
            appt.setCustomerId(monthlyRS.getInt("customerId"));
            appt.setTitle(monthlyRS.getString("title"));
            appt.setLocation(monthlyRS.getString("location"));
            appt.setType(monthlyRS.getString("type"));
            appt.setDate(localStartDate);
            appt.setStartTime(localStartTime);
            appt.setEndTime(localEndTime);

            appointmentsMonthly.add(appt);
        }

        return appointmentsMonthly;
    }

    public static ObservableList<Appointment> getAppointmentsByUser() throws SQLException
    {
        ObservableList<Appointment> allApptTimes = FXCollections.observableArrayList();
        Statement statement = DBQuery.getStatement();
        Integer userId = LoginScreenController.currentUser.getUserId();
        String userQuery = "SELECT * FROM appointment WHERE userId = " + userId + ";";
        statement.execute(userQuery);
        ResultSet rs = statement.getResultSet();
        while (rs.next())
        {
            Appointment appt = new Appointment();

            LocalDate startDate = rs.getDate("start").toLocalDate();
            LocalTime startTime = rs.getTime("start").toLocalTime();
            LocalDateTime start = startTime.atDate(startDate);
            ZonedDateTime startDatetimeUTC = start.atZone(zoneUTC);
            ZonedDateTime startDatetimeZoned = startDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalDate localStartDate = startDatetimeZoned.toLocalDate();
            LocalTime localStartTime = startDatetimeZoned.toLocalTime();
            LocalDate endDate = rs.getDate("end").toLocalDate();
            LocalTime endTime = rs.getTime("end").toLocalTime();
            LocalDateTime end = endTime.atDate(endDate);
            ZonedDateTime endDatetimeUTC = end.atZone(zoneUTC);
            ZonedDateTime endDatetimeZoned = endDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalTime localEndTime = endDatetimeZoned.toLocalTime();

            appt.setAppointmentId(rs.getInt("appointmentId"));
            appt.setCustomerId(rs.getInt("customerId"));
            appt.setTitle(rs.getString("title"));
            appt.setLocation(rs.getString("location"));
            appt.setType(rs.getString("type"));
            appt.setDate(localStartDate);
            appt.setStartTime(localStartTime);
            appt.setStart(start);
            appt.setEndTime(localEndTime);

            allApptTimes.add(appt);
        }
        return allApptTimes;
    }

    public static ObservableList<Appointment> getAppointmentsByUser(User user) throws SQLException
    {
        ObservableList<Appointment> allApptTimes = FXCollections.observableArrayList();
        Statement statement = DBQuery.getStatement();
        Integer userId = getUserId(user.getUserName());
        String userQuery = "SELECT * FROM appointment WHERE userId = " + userId + ";";
        statement.execute(userQuery);
        ResultSet rs = statement.getResultSet();
        while (rs.next())
        {
            Appointment appt = new Appointment();
            LocalDate startDate = rs.getDate("start").toLocalDate();
            LocalTime startTime = rs.getTime("start").toLocalTime();
            LocalDateTime start = startTime.atDate(startDate);
            ZonedDateTime startDatetimeUTC = start.atZone(zoneUTC);
            ZonedDateTime startDatetimeZoned = startDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalDate localStartDate = startDatetimeZoned.toLocalDate();
            LocalTime localStartTime = startDatetimeZoned.toLocalTime();
            LocalDate endDate = rs.getDate("end").toLocalDate();
            LocalTime endTime = rs.getTime("end").toLocalTime();
            LocalDateTime end = endTime.atDate(endDate);
            ZonedDateTime endDatetimeUTC = end.atZone(zoneUTC);
            ZonedDateTime endDatetimeZoned = endDatetimeUTC.withZoneSameInstant(defaultZoneId);
            LocalTime localEndTime = endDatetimeZoned.toLocalTime();

            appt.setAppointmentId(rs.getInt("appointmentId"));
            appt.setCustomerId(rs.getInt("customerId"));
            appt.setTitle(rs.getString("title"));
            appt.setLocation(rs.getString("location"));
            appt.setType(rs.getString("type"));
            appt.setDate(localStartDate);
            appt.setStartTime(localStartTime);
            appt.setStart(start);
            appt.setEndTime(localEndTime);

            allApptTimes.add(appt);
        }
        return allApptTimes;
    }

    public static ObservableList<Appointment> getCustomerAppts(Customer customer) throws SQLException
    {
        ObservableList<Appointment> appts = FXCollections.observableArrayList();
        Statement statement = DBQuery.getStatement();
        String query = "SELECT * FROM appointment WHERE customerId = " + customer.getCustomerId() + " AND start > NOW();";
        statement.execute(query);
        ResultSet rs = statement.getResultSet();
        while (rs.next())
        {
            Appointment appt = new Appointment();
            appt.setAppointmentId(rs.getInt("appointmentId"));
            appts.add(appt);
        }
        return appts;
    }
}
