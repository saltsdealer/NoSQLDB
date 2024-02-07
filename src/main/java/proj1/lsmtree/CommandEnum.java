package proj1.lsmtree;
/*
 *@Author : Tairan Ren
 *@Date   : 2024/2/7 0:07
 *@Title  :
 */


/**
 * Enum representing various database commands.
 * Each command is associated with a unique flag to distinguish between different operations.
 */
public enum CommandEnum {
  // Enum constants
  DELETE(-1), // Represents a delete operation with a flag value of -1
  SET(0),     // Represents a set operation with a flag value of 0
  INSERT(1),  // Represents an insert operation with a flag value of 1
  SEARCH(2);  // Represents a search operation with a flag value of 2

  private int flag; // Flag associated with each command

  /**
   * Private constructor for enum constants.
   * @param flag The flag value associated with the command.
   */
  CommandEnum(int flag) {
    this.flag = flag;
  }

  /**
   * Returns the enum constant of this type with the specified flag.
   * If no constant with the specified flag is found, throws IllegalArgumentException.
   *
   * @param flag The flag value of the command.
   * @return CommandEnum The enum constant with the specified flag.
   * @throws IllegalArgumentException if the flag does not correspond to any enum constant.
   */
  public static CommandEnum valueOf(int flag) {
    for (CommandEnum command : CommandEnum.values()) { // Loop through all enum constants
      if (command.flag == flag) { // Check if the flag matches the current enum constant
        return command; // Return the matching enum constant
      }
    }
    // If no matching flag is found, throw an exception
    throw new IllegalArgumentException("Unsupported flag: " + flag);
  }

  /**
   * Returns the flag value associated with the enum constant.
   *
   * @return The flag value of the enum constant.
   */
  public int getFlag() {
    return this.flag;
  }
}