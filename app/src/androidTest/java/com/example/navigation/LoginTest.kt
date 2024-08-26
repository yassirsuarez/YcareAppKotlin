package com.example.navigation
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.*

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun testLoginSuccess() {
        onView(withId(R.id.username)).perform(typeText("prova@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.password)).perform(typeText("123456"), closeSoftKeyboard())

    onView(withId(R.id.accedi)).perform(click())

       onView(withText("Accesso consentito")).inRoot(withDecorView(not(isRoot())))
            .check(matches(isDisplayed()))

       onView(withId(R.id.fragment_container)).check(matches(isDisplayed()))
    }

    @Test
    fun testLoginFailure() {
        onView(withId(R.id.username)).perform(typeText("prova@example.com"), closeSoftKeyboard())
        onView(withId(R.id.password)).perform(typeText("wrongpassword"), closeSoftKeyboard())

        onView(withId(R.id.accedi)).perform(click())

        onView(withText(containsString("Accesso fallito:"))).inRoot(withDecorView(not(isRoot())))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testRedirectToSignUp() {
       onView(withId(R.id.registra)).perform(click())

      onView(withId(R.id.Registrazione)).check(matches(isDisplayed()))
    }
    }

