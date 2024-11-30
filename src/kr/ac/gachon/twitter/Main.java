package kr.ac.gachon.twitter;

import javax.swing.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // GUI 모드로 시작할지 콘솔 모드로 시작할지 선택
        System.out.println("Select mode:");
        System.out.println("1 - GUI Mode");
        System.out.println("2 - Console Mode");
        
        Scanner scanner = new Scanner(System.in);
        int mode = scanner.nextInt();
        
        if (mode == 1) {
            // GUI 모드
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        } else {
            // 콘솔 모드 - 기존 코드 유지
            UserService userService = new UserService();
            PostService postService = new PostService();
            String userId = null;
            
            while (true) {
                System.out.println("Select an option:");
                System.out.println("0 - Log in");
                System.out.println("1 - Sign up");
                int op1 = scanner.nextInt();

                if (op1 == 0) {
                    userId = userService.logIn(scanner);
                    if (userId != null) {
                        System.out.println("Logged in successfully as " + userId);
                        break;
                    } else {
                        System.out.println("Log in failed. Try again.");
                    }
                } else if (op1 == 1) {
                    userService.signUp(scanner);
                } else {
                    System.out.println("Invalid option. Please try again.");
                }
            }

            while (userId != null) {
                System.out.println("Select an option:");
                System.out.println("2 - Write a post");
                System.out.println("3 - Like a post");
                System.out.println("4 - Follow a user");
                System.out.println("5 - Display all users");
                System.out.println("6 - Comment on a post");
                System.out.println("7 - Like a comment");
                System.out.println("8 - Reply to a comment");
                System.out.println("9 - Like a reply");
                System.out.println("10 - Scrap a post");
                System.out.println("11 - Send a message");
                int op2 = scanner.nextInt();

                switch (op2) {
                    case 2:
                        DatabaseServer db = new DatabaseServer();
                        User user = db.getUserById(userId);
                        if (user != null) {
                            postService.openWritePostGUI(user);
                        } else {
                            System.out.println("User not found");
                        }
                        break;
                    case 3:
                        postService.likePost(scanner, userId);
                        break;
                    case 4:
                        userService.followUser(scanner, userId);
                        break;
                    case 5:
                        userService.displayAllUsers();
                        break;
                    case 6:
                        postService.writeComment(scanner, userId);
                        break;
                    case 7:
                        postService.likeComment(scanner, userId);
                        break;
                    case 8:
                        postService.writeReply(scanner, userId);
                        break;
                    case 9:
                        postService.likeReply(scanner, userId);
                        break;
                    case 10:
                        postService.scrapPost(scanner, userId);
                        break;
                    case 11:
                        userService.sendMessage(scanner, userId);
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            }
        }
    }
}
