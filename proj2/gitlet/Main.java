package gitlet;

import java.util.Arrays;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main {

    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length >= 1) {
            switch (args[0]) {
                case "init": {
                    GitletCLI.gitInit();
                    break;
                }
                case "status": {
                    GitletCLI.gitStatus();
                    break;
                }
                case "add": {
                    GitletCLI.gitAdd(Arrays.copyOfRange(args, 1, args.length));
                    break;
                }
                case "commit": {
                    GitletCLI.gitCommit(Arrays.copyOfRange(args, 1, args.length));
                    break;
                }
                case "log": {
                    GitletCLI.gitLog();
                    break;
                }
                case "global-log": {
                    GitletCLI.gitGlobalLog();
                    break;
                }
                case "find": {
                    GitletCLI.gitFind(Arrays.copyOfRange(args, 1, args.length));
                    break;
                }
                case "checkout": {
                    GitletCLI.gitCheckout(Arrays.copyOfRange(args, 1, args.length));
                    break;
                }
                case "rm": {
                    GitletCLI.gitRm(Arrays.copyOfRange(args, 1, args.length));
                    break;
                }
                case "branch": {
                    GitletCLI.gitBranch(Arrays.copyOfRange(args, 1, args.length));
                    break;
                }
                case "reset": {
                    GitletCLI.gitReset(Arrays.copyOfRange(args, 1, args.length));
                    break;
                }
                case "merge": {
                    GitletCLI.gitMerge(Arrays.copyOfRange(args, 1, args.length));
                    break;
                }
                case "rm-branch": {
                    GitletCLI.gitRmBranch(Arrays.copyOfRange(args, 1, args.length));
                    break;
                }
                default: {
                    System.out.println("No command with that name exists.");
                    break;
                }
            }
        } else {
            System.out.println("Please enter a command.");
        }
    }
}
