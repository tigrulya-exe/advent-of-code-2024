package exe.tigrulya.day17;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static exe.tigrulya.Utils.getResource;
import static exe.tigrulya.day17.Task2.ComboOperandSupport.asCombo;

public class Task2 {

    public enum Register {
        A, B, C
    }

    public record Registers(Map<Register, Long> registers) {
        public void setRegisterValue(Register name, long value) {
            registers.put(name, value);
        }

        public long getRegisterValue(Register name) {
            return Optional.ofNullable(registers.get(name))
                    .orElseThrow();
        }

        public Registers copy() {
            return new Registers(new HashMap<>(registers));
        }
    }

    public static class ComboOperandSupport {
        public static long asCombo(Registers registers, byte operand) {
            return switch (operand) {
                case 0, 1, 2, 3 -> operand;
                case 4 -> registers.getRegisterValue(Register.A);
                case 5 -> registers.getRegisterValue(Register.B);
                case 6 -> registers.getRegisterValue(Register.C);
                default -> throw new IllegalStateException("Unexpected value: " + operand);
            };
        }
    }

    public sealed interface Result {
        record NoOutput() implements Result {
            public static final NoOutput INSTANCE = new NoOutput();
        }

        record Output(byte value) implements Result {
        }

        record Jump(int newPointer) implements Result {
        }

        static Result noOutput() {
            return NoOutput.INSTANCE;
        }

        static Result output(byte value) {
            return new Output(value);
        }

        static Result jump(int newPointer) {
            return new Jump(newPointer);
        }
    }

    public enum Instruction {
        ADV(0) {
            @Override
            Result run(Registers registers, byte operand) {
                return dv(registers, operand, Register.A);
            }
        },
        BXL(1) {
            @Override
            Result run(Registers registers, byte operand) {
                long result = registers.getRegisterValue(Register.B) ^ operand;
                registers.setRegisterValue(Register.B, result);
                return Result.noOutput();
            }
        },
        BST(2) {
            @Override
            Result run(Registers registers, byte operand) {
                long result = asCombo(registers, operand) % 8;
                registers.setRegisterValue(Register.B, result);
                return Result.noOutput();
            }
        },
        JNZ(3) {
            @Override
            Result run(Registers registers, byte operand) {
                return registers.getRegisterValue(Register.A) == 0
                        ? Result.noOutput()
                        : Result.jump(operand);
            }
        },
        BXC(4) {
            @Override
            Result run(Registers registers, byte operand) {
                long result = registers.getRegisterValue(Register.B)
                        ^ registers.getRegisterValue(Register.C);
                registers.setRegisterValue(Register.B, result);
                return Result.noOutput();
            }
        },
        OUT(5) {
            @Override
            Result run(Registers registers, byte operand) {
                byte result = (byte) (asCombo(registers, operand) % 8);
                return Result.output(result);
            }
        },
        BDV(6) {
            @Override
            Result run(Registers registers, byte operand) {
                return dv(registers, operand, Register.B);
            }
        },
        CDV(7) {
            @Override
            Result run(Registers registers, byte operand) {
                return dv(registers, operand, Register.C);
            }
        };

        private final byte code;

        Instruction(int code) {
            this.code = (byte) code;
        }

        abstract Result run(Registers registers, byte operand);

        // i'm lazy to use map here
        static Instruction from(byte code) {
            for (var instruction : Instruction.values()) {
                if (instruction.code == code) {
                    return instruction;
                }
            }

            throw new IllegalArgumentException("Unknown code " + code);
        }

        protected Result dv(Registers registers, byte operand, Register outRegister) {
            double result = (double) registers.getRegisterValue(Register.A)
                    / (1 << asCombo(registers, operand));
            registers.setRegisterValue(outRegister, (long) result);
            return Result.noOutput();
        }
    }

    public static class Program {
        private final List<Byte> encodedProgram;
        private final Registers registers;

        public Program(List<Byte> encodedProgram, Registers registers) {
            this.encodedProgram = encodedProgram;
            this.registers = registers;
        }

        public long findRecursiveA() {
            return findRecursiveA(0L, 1)
                    .orElseThrow();
        }

        public Optional<Long> findRecursiveA(long recursiveA, int depth) {
            System.out.println("IN:   Recursive A: " + recursiveA + " depth: " + depth);
            if (depth > encodedProgram.size()) {
                return Optional.of(recursiveA >> 3);
            }

            for (int i = 0; i < 8; ++i) {
                List<Byte> output = run(recursiveA + i);

                System.out.println("Trying " + Long.toOctalString(recursiveA + i)
                        + " should be: " + encodedProgram.subList(encodedProgram.size() - depth, encodedProgram.size())
                        + " but is: " + output);

                if (output.equals(encodedProgram.subList(encodedProgram.size() - depth, encodedProgram.size()))) {
                    System.out.println("FROM BASE " + (recursiveA + i) + " TO: " + ((recursiveA + i) << 3));
                    Optional<Long> maybeA = findRecursiveA((recursiveA + i) << 3, depth + 1);
                    if (maybeA.isPresent()) {
                        return maybeA;
                    }
                }
            }

            return Optional.empty();
        }

        public List<Byte> run(long aRegister) {
            List<Byte> output = new ArrayList<>();
            int instructionPointer = 0;
            Registers localRegisters = registers.copy();
            localRegisters.setRegisterValue(Register.A, aRegister);

            while (instructionPointer < encodedProgram.size()) {
                var instruction = Instruction.from(encodedProgram.get(instructionPointer));
                Result result = instruction.run(localRegisters, encodedProgram.get(instructionPointer + 1));

                if (result instanceof Result.Jump jmp) {
                    instructionPointer = jmp.newPointer;
                    continue;
                }

                if (result instanceof Result.Output out) {
                    output.add(out.value);
                }

                instructionPointer += 2;
            }
            return output;
        }
    }

    public static void main(String[] args) throws IOException {
        boolean delimiterHandled = false;

        Registers registers = new Registers(new HashMap<>());
        List<Byte> encodedProgram = new ArrayList<>();

        try (var lines = Files.lines(getResource("input/17.txt"))) {
            Iterable<String> linesIterable = lines::iterator;
            for (var line : linesIterable) {
                if (line.isBlank()) {
                    delimiterHandled = true;
                    continue;
                }

                if (!delimiterHandled) {
                    parseRegister(line, registers);
                    continue;
                }

                encodedProgram = Stream.of(line.replace("Program: ", "")
                                .split(","))
                        .map(Byte::parseByte)
                        .toList();
                break;
            }

            long result = new Program(encodedProgram, registers).findRecursiveA();
            System.out.println("Result: " + result);
        }
    }

    private static void parseRegister(String value, Registers registers) {
        String[] split = value.replaceAll("(Register |:)", "")
                .split(" ");
        registers.setRegisterValue(
                Register.valueOf(split[0]),
                Integer.parseInt(split[1]));
    }
}

