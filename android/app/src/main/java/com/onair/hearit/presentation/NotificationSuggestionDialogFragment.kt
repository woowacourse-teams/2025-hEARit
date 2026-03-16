package com.onair.hearit.presentation

import android.app.Dialog
import android.os.Bundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import com.onair.hearit.R
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitPurple1
import com.onair.hearit.presentation.theme.HearitTypoGraphy

class NotificationSuggestionDialogFragment : DialogFragment() {
    var onConfirm: (() -> Unit)? = null
    var onDismiss: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())

        val composeView: ComposeView =
            ComposeView(requireContext()).apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
                )
                setContent {
                    NotificationSuggestionDialog(
                        onConfirm = {
                            onConfirm?.invoke()
                            dismiss()
                        },
                        onDismiss = {
                            onDismiss?.invoke()
                            dismiss()
                        },
                    )
                }
            }
        dialog.setContentView(composeView)
        return dialog
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSuggestionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = HearitBlack,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_news),
                    contentDescription = null,
                    modifier = Modifier.size(140.dp),
                )

                Spacer(modifier = Modifier.height(34.dp))

                Text(
                    text =
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = HearitPurple1)) {
                                append("히어릿")
                            }
                            withStyle(SpanStyle(color = Gray4)) {
                                append("이 아침 인사하러 왔어요!")
                            }
                        },
                    style = HearitTypoGraphy.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "매일 아침, 새로운 히어릿을 앱 푸시로 알려드릴게요 ☀️",
                    color = Gray2,
                    style = HearitTypoGraphy.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(54.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(42.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(0.5.dp, Gray4),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = Gray4,
                            ),
                    ) {
                        Text(
                            text = "안 받을래요",
                            style = HearitTypoGraphy.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(42.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = HearitPurple1,
                                contentColor = Gray4,
                            ),
                    ) {
                        Text(
                            text = "알림 받을래요",
                            style = HearitTypoGraphy.titleSmall,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun NotificationSuggestionDialogPreview() {
    Surface {
        NotificationSuggestionDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}
