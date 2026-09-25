package ru.mirea.project;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.RequestStatus;
import ru.mirea.project.model.SecurityRequest;
import ru.mirea.project.service.SecurityRequestService;
import ru.mirea.project.util.ExcelExporter;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final SecurityRequestService service = new SecurityRequestService();

    public static void main(String[] args) {
        while (true) {
            printMenu();
            int choice = readIntSafe("Выберите действие: ");

            switch (choice) {
                case 1 -> createRequest();
                case 2 -> showAllRequests();
                case 3 -> findById();
                case 4 -> updateStatus();
                case 5 -> deleteRequest();
                case 6 -> searchAndFilter();
                case 7 -> service.printStatistics();
                case 8 -> exportData();
                case 0 -> {
                    System.out.println("Выход!!!");
                    return;
                }
                default -> System.out.println("Неверный пункт меню.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n===== СИСТЕМА ОХРАННЫХ УСЛУГ =====");
        System.out.println("1. Создать заявку");
        System.out.println("2. Показать все заявки");
        System.out.println("3. Найти заявку по ID");
        System.out.println("4. Изменить статус заявки");
        System.out.println("5. Удалить заявку");
        System.out.println("6. Поиск, фильтрация и сортировка");
        System.out.println("7. Статистика");
        System.out.println("8. Экспорт данных в CSV");
        System.out.println("0. Выход");
    }

    private static int readIntSafe(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: ID должен быть целым числом. Попробуйте снова.");
            }
        }
    }

    private static void createRequest() {
        try {
            int userId = readIntSafe("Введите ID клиента: ");
            System.out.print("Введите адрес объекта: ");
            String address = scanner.nextLine();
            System.out.print("Введите тип охраны (Физическая/Пультовая): ");
            String type = scanner.nextLine();
            service.createRequest(userId, address, type);
        } catch (BusinessException e) {
            System.out.println("Ошибка бизнес-логики: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void showAllRequests() {
        List<SecurityRequest> list = service.getAllRequests();
        if (list.isEmpty())
            System.out.println("Список пуст.");
        else
            list.forEach(System.out::println);
    }

    private static void findById() {
        int id = readIntSafe("Введите ID заявки: ");
        try {
            SecurityRequest req = service.getRequestById(id);
            System.out.println(req);
        } catch (EntityNotFoundException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void updateStatus() {
        int id = readIntSafe("Введите ID заявки: ");
        System.out.print("Введите новый статус (NEW, IN_PROGRESS, DONE, CANCELLED): ");
        String status = scanner.nextLine();
        try {
            service.updateStatus(id, status);
        } catch (EntityNotFoundException | BusinessException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void deleteRequest() {
        int id = readIntSafe("Введите ID заявки для удаления: ");
        try {
            service.deleteRequest(id);
        } catch (EntityNotFoundException | BusinessException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void searchAndFilter() {
        System.out.print("Введите часть адреса для поиска (или Enter для пропуска): ");
        String address = scanner.nextLine();
        if (address.isEmpty())
            address = null;

        System.out.print("Введите статус для фильтра (NEW, IN_PROGRESS, DONE, CANCELLED) или Enter: ");
        String statusStr = scanner.nextLine();
        RequestStatus status = null;
        if (!statusStr.isEmpty()) {
            try {
                status = RequestStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Неверный статус, фильтр пропущен.");
            }
        }

        System.out.print("Сортировать по дате? (1 - Да, 0 - Нет): ");
        boolean sortByDate = scanner.nextLine().equals("1");

        List<SecurityRequest> result = service.searchAndFilter(address, status, sortByDate);
        result.forEach(System.out::println);
    }

    private static void exportData() {
        List<SecurityRequest> list = service.getAllRequests();
        ExcelExporter.exportToCsv(list, "requests_export.csv");
    }
}