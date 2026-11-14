package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TaskFlow", appName)
  }

  @Test
  fun `quadrant classification maps importance and urgency accurately`() {
    assertEquals(
      com.example.data.Quadrant.Q1_DO_FIRST,
      com.example.data.Quadrant.fromImportanceAndUrgency(isImportant = true, isUrgent = true)
    )
    assertEquals(
      com.example.data.Quadrant.Q2_SCHEDULE,
      com.example.data.Quadrant.fromImportanceAndUrgency(isImportant = true, isUrgent = false)
    )
    assertEquals(
      com.example.data.Quadrant.Q3_DELEGATE,
      com.example.data.Quadrant.fromImportanceAndUrgency(isImportant = false, isUrgent = true)
    )
    assertEquals(
      com.example.data.Quadrant.Q4_ELIMINATE,
      com.example.data.Quadrant.fromImportanceAndUrgency(isImportant = false, isUrgent = false)
    )
  }
}
