# StepProgressView

<img src="https://img.shields.io/badge/status-development-brightgreen"/>

A library to show a tracking status of a process

![Screenshot](https://github.com/AbrahamCuautle/StepProgressView/blob/main/screenshots/demo-step-progress-view.gif)

## Usage
```xml

<com.abrahamcuautle.stepprogressview.StepProgressView
            android:id="@+id/step_progress_view"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:layout_marginVertical="16dp"
            app:spv_radius="16dp"
            app:spv_primary_progress_color="@color/design_default_color_primary"
            app:spv_secondary_progress_color="@android:color/darker_gray"
            app:spv_tick_color="@android:color/white"
            app:spv_number_text_color="@android:color/black"
            app:spv_number_text_size="14dp"
            app:spv_number_font_family="@font/inter_bold"
            app:spv_spacing_step_and_title="8dp"
            app:spv_title_text_color="@android:color/darker_gray"
            app:spv_title_text_size="14dp"
            app:spv_title_font_family="@font/inter_bold"
            app:spv_titles="@array/spv_titiles"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toBottomOf="parent"/>

```
To select a step position call

```java
     step_progress_view.setStepPosition(index_position)
```

You can also listen step position changes

```java
    step_progress_view.setOnStepChangedListener(new OnStepChangedListener() {
        @Override
        public void onStepChanged(StepProgressView stepProgressView, int position); {

        }
    })
```

## Customize

There's a set of methods you can use to customize the view
| Property | Usage |
| ------------- |:-------: |
| `setPrimaryProgressColor` | |
| `setSecondaryProgressColor` | |
| `setTickTintColor` | |
| `setNumberTextColor`| |
| `setNumberTypeface`| |
| `setNumberTextSize`| |
| `setTitlesTextColor`| |
| `setTitlesTypeface`| |
| `setTitlesTextSize`| |
| `setSpacingStepAndTitle`| |
| `setRadius`| |

