package dk.ek.gameapi.service;

import dk.ek.gameapi.entity.Designer;
import dk.ek.gameapi.exception.ResourceNotFoundException;
import dk.ek.gameapi.repository.DesignerRepository;
import dk.ek.gameapi.dto.DesignerRequest;
import dk.ek.gameapi.dto.DesignerResponse;
import dk.ek.gameapi.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesignerService {

    private final DesignerRepository designerRepository;

    public PagedResponse<DesignerResponse> getDesigners(String search, Pageable pageable) {
        Page<Designer> designers;
        if (search != null && !search.isBlank()) {
            designers = designerRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            designers = designerRepository.findAll(pageable);
        }
        return PagedResponse.from(designers, DesignerResponse::from);
    }

    public DesignerResponse getDesignerById(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid designer ID: " + id);
        }
        Designer designer = designerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designer", id));
        return DesignerResponse.from(designer);
    }

    @Transactional
    public DesignerResponse createDesigner(DesignerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Designer request cannot be null");
        }
        Designer designer = new Designer();
        updateDesignerFromRequest(designer, request);
        return DesignerResponse.from(designerRepository.save(designer));
    }

    @Transactional
    public DesignerResponse updateDesigner(Integer id, DesignerRequest request) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid designer ID: " + id);
        }
        Designer designer = designerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designer", id));
        updateDesignerFromRequest(designer, request);
        return DesignerResponse.from(designerRepository.save(designer));
    }

    @Transactional
    public void deleteDesigner(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid designer ID: " + id);
        }
        if (!designerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Designer", id);
        }
        designerRepository.deleteById(id);
    }

    private void updateDesignerFromRequest(Designer designer, DesignerRequest request) {
        designer.setName(request.name());
        designer.setDob(request.dob());
    }
}
