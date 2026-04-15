package com.library.lms.config;

import com.library.lms.model.Book;
import com.library.lms.model.Member;
import com.library.lms.model.User;
import com.library.lms.repository.BookRepository;
import com.library.lms.repository.MemberRepository;
import com.library.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedBooks();
        seedMembers();
    }

    private void seedUsers() {
        createUserIfMissing("admin", "admin@library.com", "admin123", Set.of("ADMIN", "LIBRARIAN"));
        createUserIfMissing("librarian", "librarian@library.com", "lib123", Set.of("LIBRARIAN"));
        createUserIfMissing("reader", "reader@library.com", "reader123", Set.of("USER"));
    }

    private void createUserIfMissing(String username, String email, String rawPassword, Set<String> roles) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.save(User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .roles(roles)
                    .enabled(true)
                    .build());
        }
    }

    private void seedBooks() {
        if (bookRepository.count() == 0) {
            bookRepository.save(Book.builder().title("Clean Code").author("Robert C. Martin")
                    .isbn("978-0132350884").category("Programming").publisher("Prentice Hall")
                    .publishedYear(2008).totalCopies(5).availableCopies(5)
                    .description("A handbook of agile software craftsmanship.").status(Book.BookStatus.AVAILABLE).build());

            bookRepository.save(Book.builder().title("The Pragmatic Programmer").author("David Thomas, Andrew Hunt")
                    .isbn("978-0135957059").category("Programming").publisher("Addison-Wesley")
                    .publishedYear(2019).totalCopies(3).availableCopies(3)
                    .description("From journeyman to master.").status(Book.BookStatus.AVAILABLE).build());

            bookRepository.save(Book.builder().title("Design Patterns").author("Gang of Four")
                    .isbn("978-0201633610").category("Software Engineering").publisher("Addison-Wesley")
                    .publishedYear(1994).totalCopies(4).availableCopies(4)
                    .description("Elements of reusable object-oriented software.").status(Book.BookStatus.AVAILABLE).build());

            bookRepository.save(Book.builder().title("To Kill a Mockingbird").author("Harper Lee")
                    .isbn("978-0061935466").category("Fiction").publisher("HarperCollins")
                    .publishedYear(1960).totalCopies(6).availableCopies(6)
                    .description("A story of racial injustice and moral growth.").status(Book.BookStatus.AVAILABLE).build());

            bookRepository.save(Book.builder().title("1984").author("George Orwell")
                    .isbn("978-0451524935").category("Fiction").publisher("Signet Classics")
                    .publishedYear(1949).totalCopies(4).availableCopies(4)
                    .description("A dystopian novel about totalitarianism.").status(Book.BookStatus.AVAILABLE).build());

            bookRepository.save(Book.builder().title("Sapiens").author("Yuval Noah Harari")
                    .isbn("978-0062316097").category("History").publisher("Harper")
                    .publishedYear(2011).totalCopies(3).availableCopies(3)
                    .description("A brief history of humankind.").status(Book.BookStatus.AVAILABLE).build());

            bookRepository.save(Book.builder().title("Introduction to Algorithms").author("CLRS")
                    .isbn("978-0262033848").category("Computer Science").publisher("MIT Press")
                    .publishedYear(2009).totalCopies(2).availableCopies(2)
                    .description("The classic algorithms textbook.").status(Book.BookStatus.AVAILABLE).build());

            bookRepository.save(Book.builder().title("Atomic Habits").author("James Clear")
                    .isbn("978-0735211292").category("Self-Help").publisher("Avery")
                    .publishedYear(2018).totalCopies(5).availableCopies(5)
                    .description("An easy and proven way to build good habits.").status(Book.BookStatus.AVAILABLE).build());

            bookRepository.save(Book.builder().title("The Great Gatsby").author("F. Scott Fitzgerald")
                    .isbn("978-0743273565").category("Fiction").publisher("Scribner")
                    .publishedYear(1925).totalCopies(4).availableCopies(4)
                    .description("A classic novel of the Jazz Age.").status(Book.BookStatus.AVAILABLE).build());

            bookRepository.save(Book.builder().title("Spring Boot in Action").author("Craig Walls")
                    .isbn("978-1617292545").category("Programming").publisher("Manning")
                    .publishedYear(2016).totalCopies(3).availableCopies(3)
                    .description("A developer-focused guide to Spring Boot.").status(Book.BookStatus.AVAILABLE).build());
        }
    }

    private void seedMembers() {
        if (memberRepository.count() == 0) {
            memberRepository.save(Member.builder()
                    .firstName("Alice").lastName("Johnson")
                    .email("alice@example.com").phone("555-1234")
                    .membershipId("MEM-ALICE001").membershipType(Member.MembershipType.PREMIUM)
                    .status(Member.MemberStatus.ACTIVE)
                    .membershipExpiry(LocalDate.now().plusYears(1))
                    .activeBorrowings(0).totalBorrowings(3).build());

            memberRepository.save(Member.builder()
                    .firstName("Bob").lastName("Smith")
                    .email("bob@example.com").phone("555-5678")
                    .membershipId("MEM-BOB0001").membershipType(Member.MembershipType.STANDARD)
                    .status(Member.MemberStatus.ACTIVE)
                    .membershipExpiry(LocalDate.now().plusMonths(6))
                    .activeBorrowings(0).totalBorrowings(1).build());

            memberRepository.save(Member.builder()
                    .firstName("Carol").lastName("Williams")
                    .email("carol@example.com").phone("555-9012")
                    .membershipId("MEM-CAROL01").membershipType(Member.MembershipType.STUDENT)
                    .status(Member.MemberStatus.ACTIVE)
                    .membershipExpiry(LocalDate.now().plusYears(2))
                    .activeBorrowings(0).totalBorrowings(0).build());
        }
    }
}
