package com.BrainStack.Services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.BrainStack.Dto.ChatbotRequest;
import com.BrainStack.Dto.ChatbotResponse;
import com.BrainStack.Dto.ModerationResponse;
import com.BrainStack.Entity.Discussion;
import com.BrainStack.Entity.Evenement;
import com.BrainStack.Entity.Groupe;
import com.BrainStack.Entity.Message;
import com.BrainStack.Entity.Recommendation;
import com.BrainStack.Entity.User;
import com.BrainStack.Repository.DiscussionRepository;
import com.BrainStack.Repository.EvenementRepository;
import com.BrainStack.Repository.GroupeRepository;
import com.BrainStack.Repository.MessageRepository;
import com.BrainStack.Repository.RecommendationRepository;
import com.BrainStack.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrainStackService {

    private final EvenementRepository evenementRepository;
    private final GroupeRepository groupeRepository;
    private final MessageRepository messageRepository;
    private final DiscussionRepository discussionRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final ModerationService moderationService;
    private final ChatbotService chatbotService;

    public List<Evenement> listEvenements() { return evenementRepository.findAll(); }
    public Optional<Evenement> getEvenement(Long id) { return evenementRepository.findById(id); }
    public Evenement saveEvenement(Evenement e) { 
        if (e.getCreatedAt() == null) {
            e.setCreatedAt(LocalDateTime.now());
        }
        return evenementRepository.save(e); 
    }
    public void deleteEvenement(Long id) { evenementRepository.deleteById(id); }

    public List<Groupe> listGroupes() { return groupeRepository.findAll(); }
    public Optional<Groupe> getGroupe(Long id) { return groupeRepository.findById(id); }
    public Groupe saveGroupe(Groupe g) { return groupeRepository.save(g); }
    public void deleteGroupe(Long id) { groupeRepository.deleteById(id); }

    public List<Message> listMessages() { return messageRepository.findAll(); }
    public Optional<Message> getMessage(Long id) { return messageRepository.findById(id); }
    
    public Message saveMessage(Message m) { 
        // Set default values for moderation
        m.setCreatedAt(LocalDateTime.now());
        m.setModerationStatus("pending");
        m.setModeratedAt(LocalDateTime.now());
        
        // Check content with AI moderation
        ModerationResponse moderationResponse = moderationService.checkContent(m.getContenu());
        
        // Update message with moderation results
        m.setModerationStatus(moderationResponse.getStatus());
        m.setModerationReason(moderationResponse.getAi_reason());
        m.setModeratedAt(LocalDateTime.now());
        
        return messageRepository.save(m); 
    }
    
    public void deleteMessage(Long id) { messageRepository.deleteById(id); }

    public List<Discussion> listDiscussions() { return discussionRepository.findAll(); }
    public Optional<Discussion> getDiscussion(Long id) { return discussionRepository.findById(id); }
    public Discussion saveDiscussion(Discussion d) { return discussionRepository.save(d); }
    public void deleteDiscussion(Long id) { discussionRepository.deleteById(id); }

    public List<Recommendation> listRecommendations() { return recommendationRepository.findAll(); }
    public Optional<Recommendation> getRecommendation(Long id) { return recommendationRepository.findById(id); }
    public void deleteRecommendation(Long id) { recommendationRepository.deleteById(id); }

    public List<User> listUsers() { return userRepository.findAll(); }
    public Optional<User> getUser(Long id) { return userRepository.findById(id); }
    public User saveUser(User u) { return userRepository.save(u); }
    public void deleteUser(Long id) { userRepository.deleteById(id); }

    // Chatbot methods
    public ChatbotResponse getChatbotResponse(ChatbotRequest request) {
        return chatbotService.getChatbotResponse(request);
    }

    // Enhanced recommendation methods
    public Recommendation saveRecommendation(Recommendation r) { 
        if (r.getCreatedAt() == null) {
            r.setCreatedAt(LocalDateTime.now());
        }
        r.setUpdatedAt(LocalDateTime.now());
        if (r.getIsActive() == null) {
            r.setIsActive(true);
        }
        if (r.getUsageCount() == null) {
            r.setUsageCount(0);
        }
        return recommendationRepository.save(r); 
    }

    public List<Recommendation> getRecommendationsByCategory(String category) {
        return recommendationRepository.findByCategoryAndIsActiveTrue(category);
    }

    public List<Recommendation> getRecommendationsByTheme(String theme) {
        return recommendationRepository.findByThemeAndIsActiveTrue(theme);
    }

    public List<Recommendation> getActiveRecommendations() {
        return recommendationRepository.findByIsActiveTrue();
    }

    public List<Recommendation> searchRecommendations(String keyword) {
        return recommendationRepository.searchRecommendations(keyword);
    }

    public List<Recommendation> getMostUsedRecommendations() {
        return recommendationRepository.findMostUsedRecommendations();
    }
}


