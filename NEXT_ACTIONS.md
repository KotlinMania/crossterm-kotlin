# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 26/48 (54.2%)
- **Function parity:** 105/435 matched (target 503) — 24.1%
- **Class/type parity:** 82/115 matched (target 159) — 71.3%
- **Combined symbol parity:** 187/550 matched (target 662) — 34.0%
- **Average inline-code cosine:** 0.30 (function body across 26 matched files)
- **Average documentation cosine:** 0.60 (doc text across 26 matched files)
- **Cheat-zeroed Files:** 8
- **Critical Issues:** 21 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. command

- **Target:** `Command`
- **Similarity:** 0.50
- **Dependents:** 2
- **Priority Score:** 2031405.0
- **Functions:** 7/9 matched
- **Missing functions:** `write_ansi`, `write_str`
- **Types:** 4/5 matched (target 4)
- **Missing types:** `Adapter`

### 2. event

- **Target:** `event.Event`
- **Similarity:** 0.35
- **Dependents:** 1
- **Priority Score:** 1166006.5
- **Functions:** 27/43 matched (target 56)
- **Missing functions:** `execute_winapi`, `is_ansi_code_supported`, `fmt`, `eq`, `hash`, `test_equality`, `test_hash`, `keycode_display`, `media_keycode_display`, `modifier_keycode_display`, `modifier_keycode_display_macos`, `modifier_keycode_display_windows`, `modifier_keycode_display_other`, `key_modifiers_display`, `event_is`, `event_as`
- **Types:** 17/17 matched (target 59)
- **Missing types:** _none_
- **Tests:** 0/11 matched

### 3. types.attribute

- **Target:** `types.Attribute [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1010310.0
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `fmt`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 4. event.read

- **Target:** `event.Read`
- **Similarity:** 0.13
- **Dependents:** 0
- **Priority Score:** 212708.8
- **Functions:** 5/24 matched (target 6)
- **Missing functions:** `eval`, `test_poll_fails_without_event_source`, `test_poll_returns_true_for_matching_event_in_queue_at_front`, `test_poll_returns_true_for_matching_event_in_queue_at_back`, `test_read_returns_matching_event_in_queue_at_front`, `test_read_returns_matching_event_in_queue_at_back`, `test_read_does_not_consume_skipped_event`, `test_try_read_does_not_consume_skipped_event`, `test_poll_timeouts_if_source_has_no_events`, `test_poll_returns_true_if_source_has_at_least_one_event`, `test_reads_returns_event_if_source_has_at_least_one_event`, `test_read_returns_events_if_source_has_events`, `test_poll_returns_false_after_all_source_events_are_consumed`, `test_poll_propagates_error`, `test_read_propagates_error`, `test_poll_continues_after_error`, `test_read_continues_after_error`, `new`, `with_events`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `InternalEventFilter`, `FakeSource`
- **Tests:** 0/19 matched

### 5. types.color

- **Target:** `types.Color [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 202510.0
- **Functions:** 4/20 matched (target 9)
- **Missing functions:** `from_str`, `serialize`, `deserialize`, `expecting`, `visit_str`, `test_known_color_conversion`, `test_unknown_color_conversion_yields_white`, `test_know_rgb_color_conversion`, `test_deserial_known_color_conversion`, `test_deserial_unknown_color_conversion`, `test_deserial_ansi_value`, `test_deserial_unvalid_ansi_value`, `test_deserial_rgb`, `test_deserial_unvalid_rgb`, `test_deserial_rgb_hex`, `test_deserial_unvalid_rgb_hex`
- **Types:** 1/5 matched (target 20)
- **Missing types:** `Error`, `Err`, `ColorVisitor`, `Value`
- **Tests:** 0/11 matched

### 6. macros

- **Target:** `Macros`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 171710.0
- **Functions:** 0/14 matched (target 5)
- **Missing functions:** `write`, `flush`, `write_ansi`, `test_queue_one`, `test_queue_many`, `test_queue_trailing_comma`, `test_execute_one`, `test_execute_many`, `test_execute_trailing_comma`, `new`, `execute_winapi`, `test_harness`, `test_queue_some`, `test_many_queues`
- **Types:** 0/3 matched (target 0)
- **Missing types:** `FakeWrite`, `FakeCommand`, `WindowsEventStream`
- **Tests:** 0/14 matched

### 7. types.colored

- **Target:** `types.Colored [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 141910.0
- **Functions:** 4/18 matched (target 12)
- **Missing functions:** `fmt`, `check_format_color`, `test_format_fg_color`, `test_format_bg_color`, `test_format_reset_fg_color`, `test_format_reset_bg_color`, `test_format_fg_rgb_color`, `test_format_fg_ansi_color`, `test_parse_ansi_fg`, `test_parse_ansi_bg`, `test_parse_ansi`, `test_parse_invalid_ansi_color`, `test`, `test_no_color`
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_
- **Tests:** 0/13 matched

### 8. clipboard

- **Target:** `Clipboard [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 91610.0
- **Functions:** 4/12 matched
- **Missing functions:** `from`, `fmt`, `from_str`, `execute_winapi`, `test_clipboard_string_to_selection`, `test_clipboard_selection_to_osc52_pc`, `test_clipboard_copy_string_osc52`, `test_clipboard_copy_string_osc52_constructor`
- **Types:** 3/4 matched (target 6)
- **Missing types:** `Err`
- **Tests:** 0/4 matched

### 9. style

- **Target:** `style.Style`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 82407.2
- **Functions:** 6/14 matched (target 18)
- **Missing functions:** `fmt`, `parse_next_u8`, `windows_always_truecolor`, `colorterm_overrides_term`, `term_24bits`, `term_256color`, `default_color_count`, `unsupported_term_colorterm_values`
- **Types:** 10/10 matched
- **Missing types:** _none_
- **Tests:** 0/6 matched

### 10. cursor

- **Target:** `cursor.Cursor`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 62407.0
- **Functions:** 2/8 matched (target 29)
- **Missing functions:** `test_move_to`, `test_move_right`, `test_move_left`, `test_move_up`, `test_move_down`, `test_save_restore_position`
- **Types:** 16/16 matched
- **Missing types:** _none_
- **Tests:** 0/6 matched

### 11. style.attributes

- **Target:** `style.Attributes`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 61705.7
- **Functions:** 10/15 matched (target 19)
- **Missing functions:** `bitand`, `bitor`, `bitxor`, `test_attributes`, `test_attributes_const`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`
- **Tests:** 0/2 matched

### 12. event.filter

- **Target:** `event.Filter`
- **Similarity:** 0.39
- **Dependents:** 0
- **Priority Score:** 51206.1
- **Functions:** 1/6 matched (target 5)
- **Missing functions:** `test_cursor_position_filter_filters_cursor_position`, `test_keyboard_enhancement_status_filter_filters_keyboard_enhancement_status`, `test_primary_device_attributes_filter_filters_primary_device_attributes`, `test_event_filter_filters_events`, `test_event_filter_filters_internal_events`
- **Types:** 6/6 matched
- **Missing types:** _none_
- **Tests:** 0/5 matched

### 13. event.timeout

- **Target:** `event.Timeout`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 50907.2
- **Functions:** 3/8 matched (target 4)
- **Missing functions:** `test_timeout_without_duration_does_not_have_leftover`, `test_timeout_without_duration_never_elapses`, `test_timeout_elapses`, `test_elapsed_timeout_has_zero_leftover`, `test_not_elapsed_timeout_has_positive_leftover`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/5 matched

### 14. event.stream

- **Target:** `event.Stream [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 50710.0
- **Functions:** 1/4 matched (target 5)
- **Missing functions:** `default`, `poll_next`, `drop`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Task`, `Item`

### 15. terminal

- **Target:** `terminal.Terminal`
- **Similarity:** 0.23
- **Dependents:** 0
- **Priority Score:** 42307.7
- **Functions:** 6/10 matched (target 17)
- **Missing functions:** `execute_winapi`, `is_ansi_code_supported`, `test_resize_ansi`, `test_raw_mode`
- **Types:** 13/13 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 16. style.stylize

- **Target:** `style.Stylize`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 20802.1
- **Functions:** 5/6 matched (target 270)
- **Missing functions:** `set_fg_bg_add_attr`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Styled`
- **Tests:** 0/1 matched

### 17. style.content_style

- **Target:** `style.ContentStyle`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 20506.8
- **Functions:** 2/4 matched (target 2)
- **Missing functions:** `as_ref`, `as_mut`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 18. style.styled_content

- **Target:** `style.StyledContentImpl`
- **Similarity:** 0.81
- **Dependents:** 0
- **Priority Score:** 10801.9
- **Functions:** 6/7 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 19. types.colors

- **Target:** `types.Colors`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 10503.4
- **Functions:** 3/4 matched
- **Missing functions:** `test_colors_then`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 20. ansi_support

- **Target:** `AnsiSupport`
- **Similarity:** 0.19
- **Dependents:** 0
- **Priority Score:** 10208.1
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `enable_vt_processing`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 21. event.internal

- **Target:** `event.Internal [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 610.0
- **Functions:** 5/5 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 6)
- **Missing types:** _none_

### 22. tty

- **Target:** `Tty`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 208.2
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 23. event.source

- **Target:** `source.Source [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched (target 1)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 24. event.sys

- **Target:** `sys.Waker [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 25. terminal.sys

- **Target:** `terminal.sys.Sys`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 26. cursor.sys

- **Target:** `sys.Sys`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |

