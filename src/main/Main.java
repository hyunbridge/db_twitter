package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
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
                    postService.openWritePostGUI(userId); // 글쓰기 GUI 호출
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
