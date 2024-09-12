package com.example.inapppurchasedemo

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.example.inapppurchasedemo.core.extensions.click
import com.example.inapppurchasedemo.core.utils.PrefUtils.isAdsRemoved
import com.example.inapppurchasedemo.databinding.FragmentHomeBinding
import com.example.inapppurchasedemo.premium.PremiumActivity

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.icPremium.visibility = if (isAdsRemoved) View.GONE else View.VISIBLE

        binding.icPremium.click {
            it.startAnimation(AnimationUtils.loadAnimation(context, R.anim.click))
            startActivity(Intent(context, PremiumActivity::class.java))
        }
    }
}