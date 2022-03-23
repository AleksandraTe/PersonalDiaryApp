package com.example.personaldiaryapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_notes_bottom_sheet.*


class NoteBottomSheetFragment : BottomSheetDialogFragment() {

    var selectedColor = "#B3B7C0"

    companion object {
        fun newInstance(): NoteBottomSheetFragment{
            val args = Bundle()
            val fragment = NoteBottomSheetFragment()
            fragment.arguments = args
            return fragment
        }
    }
    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val view = LayoutInflater.from(context).inflate(R.layout.fragment_notes_bottom_sheet, null)
        dialog.setContentView(view)

        val param = (view.parent as View).layoutParams as CoordinatorLayout.LayoutParams

        val behavior = param.behavior

        if (behavior is BottomSheetBehavior<*>) {
            behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    var state = ""
                    when (newState) {
                        BottomSheetBehavior.STATE_DRAGGING -> {
                            state = "DRAGGING"
                        }
                        BottomSheetBehavior.STATE_SETTLING -> {
                            state = "SETTLING"
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            state = "EXPANDED"
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            state = "COLLAPSED"
                        }
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            state = "HIDDEN"
                            dismiss()
                            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }

            })

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notes_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListener()
    }

    private fun sendBroadcast(action: String, color: String = ""){
        val intent = Intent("bottom_sheet_action")
        if(color != ""){
            selectedColor = color
            intent.putExtra("selectedColor", selectedColor)
        }
        intent.putExtra("action", action)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    private fun setListener(){
        fNote1.setOnClickListener {
            imgColor1.setImageResource(R.drawable.ic_checkmark)
            imgColor2.setImageResource(0)
            imgColor3.setImageResource(0)
            sendBroadcast("Gray", "#CBCCEC")
        }
        fNote2.setOnClickListener {
            imgColor1.setImageResource(0)
            imgColor2.setImageResource(R.drawable.ic_checkmark)
            imgColor3.setImageResource(0)
            sendBroadcast("Blue", "#B9CFF6")
        }
        fNote3.setOnClickListener {
            imgColor1.setImageResource(0)
            imgColor2.setImageResource(0)
            imgColor3.setImageResource(R.drawable.ic_checkmark)
            sendBroadcast("Red","#EEC8C8")
        }
        layoutAddImage.setOnClickListener {
            sendBroadcast("Image")
        }

        layoutAddCheckbox.setOnClickListener {
            sendBroadcast("Checkbox")
        }

        layoutDownload.setOnClickListener {
            sendBroadcast("Download")
        }
    }
}