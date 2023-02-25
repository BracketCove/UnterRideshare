package com.bracketcove.android.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bracketcove.android.R
import com.bracketcove.android.databinding.FragmentChatBinding
import com.bracketcove.android.databinding.FragmentPassengerDashboardBinding
import com.bracketcove.android.navigation.ChatKey
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.zhuinden.simplestackextensions.fragments.KeyedFragment
import com.zhuinden.simplestackextensions.fragmentsktx.backstack
import com.zhuinden.simplestackextensions.fragmentsktx.lookup
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory

class ChatFragment : KeyedFragment(R.layout.fragment_chat) {

    private val viewModel by lazy { lookup<ChatViewModel>() }

    lateinit var binding: FragmentChatBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)

        binding.backIcon.setOnClickListener {
            viewModel.handleBackButton()
        }

        val channelId: String = (getKey() as ChatKey).channelId
        val messageListViewModel: MessageListViewModel by viewModels {
            MessageListViewModelFactory(cid = channelId)
        }

        val messageInputViewModel: MessageInputViewModel by viewModels {
            MessageListViewModelFactory(cid = channelId)
        }

        messageListViewModel.bindView(binding.messageListView, viewLifecycleOwner)
        messageInputViewModel.bindView(binding.messageInputView, viewLifecycleOwner)
    }
}