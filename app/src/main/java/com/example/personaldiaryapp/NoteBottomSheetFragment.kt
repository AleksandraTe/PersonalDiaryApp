package com.example.personaldiaryapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private fun setListener(){
        fNote1.setOnClickListener {

            imgColor1.setImageResource(R.drawable.ic_checkmark)
            imgColor2.setImageResource(0)
            imgColor3.setImageResource(0)
            selectedColor = "#B3B7C0"

            Toast.makeText(requireContext(),selectedColor,Toast.LENGTH_SHORT)
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("selectedColor", selectedColor)
            intent.putExtra("action", "Gray")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }
        fNote2.setOnClickListener {

            imgColor1.setImageResource(0)
            imgColor2.setImageResource(R.drawable.ic_checkmark)
            imgColor3.setImageResource(0)
            selectedColor = "#B9CFF6"

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("selectedColor", selectedColor)
            intent.putExtra("action", "Blue")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }
        fNote3.setOnClickListener {

            imgColor1.setImageResource(0)
            imgColor2.setImageResource(0)
            imgColor3.setImageResource(R.drawable.ic_checkmark)
            selectedColor = "#EEC8C8"

            val intent = Intent("bottom_sheet_action")
            intent.putExtra("selectedColor", selectedColor)
            intent.putExtra("action", "Red")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }

        layoutAddImage.setOnClickListener {
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "Image")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }

        layoutAddCheckbox.setOnClickListener {
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "Checkbox")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }

        layoutDownload.setOnClickListener {
            val intent = Intent("bottom_sheet_action")
            intent.putExtra("action", "Download")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }
    }
}