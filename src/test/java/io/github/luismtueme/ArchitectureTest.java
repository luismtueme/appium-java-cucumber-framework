package io.github.luismtueme;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;

/**
 * The framework's design rules, checked on the compiled classes. A violation fails the build with the offending class
 * and line.
 */
class ArchitectureTest {

    private static JavaClasses classes;
    private static JavaClasses mainClasses;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPackages("io.github.luismtueme");
        mainClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("io.github.luismtueme");
    }

    private static void check(ArchRule rule) {
        rule.check(classes);
    }

    @Test
    void screenObjectsDoNotAssert() {
        check(noClasses()
                .that()
                .resideInAPackage("..framework.screens..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.assertj..", "org.junit..", "org.awaitility..")
                .because("assertions belong in steps and specs, so a failure points at the claim that failed"));
    }

    @Test
    void stepsUseScreenObjectsInsteadOfLocators() {
        check(
                noClasses()
                        .that()
                        .resideInAPackage("..acceptance.steps..")
                        .should()
                        .callMethod(WebDriver.class, "findElement", By.class)
                        .orShould()
                        .callMethod(WebDriver.class, "findElements", By.class)
                        .orShould()
                        .callMethod(SearchContext.class, "findElement", By.class)
                        .orShould()
                        .callMethod(SearchContext.class, "findElements", By.class)
                        .orShould()
                        .dependOnClassesThat()
                        .areAssignableTo(By.class)
                        .because(
                                "locators live in screen objects (src/main/.../screens), so a UI change is fixed in one place"));
    }

    @Test
    void nothingSleeps() {
        check(
                noClasses()
                        .should()
                        .callMethod(Thread.class, "sleep", long.class)
                        .orShould()
                        .callMethod(Thread.class, "sleep", Duration.class)
                        .orShould()
                        .callMethod(Thread.class, "sleep", long.class, int.class)
                        .because(
                                "fixed waits make tests slow and still flaky; wait for a condition (screen objects, Eventually)"));
    }

    @Test
    void noImplicitWaitsOutsideDriverFactory() {
        check(noClasses()
                .that()
                .doNotHaveFullyQualifiedName("io.github.luismtueme.framework.driver.DriverFactory")
                .should()
                .callMethod(WebDriver.Timeouts.class, "implicitlyWait", Duration.class)
                .because("implicit waits slow down every failing lookup and mix badly with explicit waits"));
    }

    @Test
    void frameworkCodeDoesNotDependOnTheTestLayer() {
        noClasses()
                .that()
                .resideInAPackage("..framework..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("io.cucumber..", "..acceptance..", "..specs..", "org.assertj..")
                .because("src/main is reusable by any runner; Cucumber glue and JUnit specs live in src/test")
                .check(mainClasses);
    }
}
