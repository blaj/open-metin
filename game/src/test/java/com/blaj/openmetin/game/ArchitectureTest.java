package com.blaj.openmetin.game;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packages = "com.blaj.openmetin.game")
public class ArchitectureTest {

  @ArchTest
  public static final ArchRule tests_should_reside_in_same_package_as_tested_class =
      classes()
          .that()
          .haveSimpleNameEndingWith("Test")
          .and()
          .haveSimpleNameNotEndingWith("IntegrationTest")
          .and()
          .haveSimpleNameNotEndingWith("ApplicationTest")
          .and()
          .doNotHaveSimpleName("ArchitectureTest")
          .and()
          .resideOutsideOfPackage("..config..")
          .should(beInSamePackageAsTestedClass())
          .because("tests should be in the same package as the production classes they test");

  private static ArchCondition<JavaClass> beInSamePackageAsTestedClass() {
    return new ArchCondition<>("be in same package as tested class") {
      @Override
      public void check(JavaClass testClass, ConditionEvents events) {
        var testClassName = testClass.getSimpleName();
        var productionClassName = testClassName.replace("Test", "");
        var testPackageName = testClass.getPackageName();

        try {
          var productionClass = Class.forName(testPackageName + "." + productionClassName);

          if (!productionClass.getPackage().getName().equals(testPackageName)) {
            var message =
                String.format(
                    "Test %s is in package %s, but class %s is in package %s",
                    testClassName,
                    testPackageName,
                    productionClassName,
                    productionClass.getPackage().getName());

            events.add(SimpleConditionEvent.violated(testClass, message));
          }
        } catch (ClassNotFoundException e) {
          var message =
              String.format(
                  "Test %s doesn't have a corresponding class %s in package %s",
                  testClassName, productionClassName, testPackageName);

          events.add(SimpleConditionEvent.violated(testClass, message));
        }
      }
    };
  }
}
