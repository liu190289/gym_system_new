# 🏋️ Gym Management System

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-red)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

**中文 README**： [README.md](README.md)

A modern, all-in-one Gym Management System built with **Java Swing** and **MySQL**. The system follows a layered architecture (**DAO / Service / UI**) with a modern look & feel (FlatLaf), covering end-to-end workflows including member services, front-desk operations, trainer functions, and back-office administration.

---

## ✨ Key Features

### 🎨 Modern UI
- **FlatLaf theme**: A flat and clean UI, improving the classic Swing appearance.
- **Role-based layout**: The main menu adapts to different roles (Member / Receptionist / Admin).
- **Visual reports**: Integrated **JFreeChart** for charts such as bar charts and pie charts.

### 👥 Multi-role Access
The system supports four roles with dedicated business views:

1. **Member**
   * 👤 **Profile**: View balance, membership validity, and personal information.
   * 📅 **Course booking**: Browse schedules and book group classes.
   * 📋 **My bookings**: View existing bookings and cancel bookings (if supported by the UI workflow).
   * 💳 **Self renewal**: Renew monthly/annual membership.

2. **Receptionist**
   * ✅ **Check-in**: Quick check-in using membership card/phone number.
   * 🛒 **POS / Shop**: Browse products, checkout, and automatically deduct stock.
   * 💰 **Recharge**: Top up member balance (with quick preset amounts).
   * 👥 **Membership service**: New member registration and membership purchase/renewal.
   * 📦 **Inventory lookup**: Check product stock status.

3. **Trainer**
   * 📝 **Attendance / Roll call**: View the booked member list for assigned courses and perform attendance confirmation.

4. **Admin**
   * 🛡️ **Full access**: Includes all receptionist and trainer capabilities.
   * 👔 **Employee management**: Onboard employees (auto account creation), offboard, reset passwords, and edit employee info.
   * 📅 **Course management**: Publish new courses, edit schedules, and remove courses.
   * 📈 **Business reports**: View revenue summaries, member growth, and inventory alerts (chart view supported).

---

## 🛠️ Tech Stack

* **Language**: Java 21
* **GUI**: Java Swing
* **UI Theme**: [FlatLaf 3.5.4](https://www.formdev.com/flatlaf/)
* **Charts**: [JFreeChart 1.5.3](https://www.jfree.org/jfreechart/)
* **Database**: MySQL 8.0+
* **DB Access**: JDBC (mysql-connector-j 9.3.0)
* **Build**: Maven
* **Date Picker**: JCalendar 1.4

---

## 🚀 Quick Start

### 1. Prerequisites
* JDK 21 or above
* MySQL 8.0 or above
* Maven 3.6+
* IntelliJ IDEA (recommended)

### 2. Database Setup
1. Create a database named `gym_system` in MySQL.
2. Locate the `database` folder under the project directory.
3. Run **`gym_system_reset_v2.sql`**.
   * This script creates tables and inserts sample data (including course times and member balances).
4. Check the DB config in `src/main/java/utils/DBUtil.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/gym_system?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";     // your DB username
private static final String PASSWORD = "your_password"; // your DB password
```

### 3. Build & Run
1. Open the project in IntelliJ IDEA.
2. Wait for Maven to download dependencies (if `JFreeChart` is red, refresh Maven).
3. Run `src/main/java/Main.java`.

---

## 🔑 Default Test Accounts

After importing `gym_system_reset_v2.sql`, you can log in with the following accounts (the plaintext password is **123456**; the database stores a hashed value, and the application hashes your input before comparison):

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `123456` | Full access (employees, reports, etc.) |
| **Receptionist** | `alice` | `123456` | Daily operations, POS, membership services |
| **Trainer** | `bob` | `123456` | Trainer role sample account |
| **Member** | `johnsmith` | `123456` | Sample member account (`member_id=1`) |
| **Member** | `sarahj` | `123456` | Sample member account (`member_id=2`) |

> 💡 **Tip**: For staff accounts, select "Employee / Staff". For member accounts, select "Member".
>
> ✅ **More sample accounts**: `carol` / `david` / `emma` (employees), `mbrown` / `emilyd` / `rwilson` / `jentaylor` / `wanderson` / `lisam` / `jesswhite` (members).
>
> ⚠️ **Note**: In the script, `jthomas` has `inactive` status, and cannot log in under the current login rule (only `status='active'` is allowed).

---

## 📂 Project Structure


