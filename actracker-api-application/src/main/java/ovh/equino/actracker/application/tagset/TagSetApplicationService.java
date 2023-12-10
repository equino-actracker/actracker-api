package ovh.equino.actracker.application.tagset;

import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tagset.*;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class TagSetApplicationService {

    private final TagSetFactory tagSetFactory;
    private final TagSetRepository tagSetRepository;
    private final TagSetDataSource tagSetDataSource;
    private final TagSetSearchEngine tagSetSearchEngine;
    private final TagSetNotifier tagSetNotifier;
    private final IdentityProvider identityProvider;

    public TagSetApplicationService(TagSetFactory tagSetFactory,
                                    TagSetRepository tagSetRepository,
                                    TagSetDataSource tagSetDataSource,
                                    TagSetSearchEngine tagSetSearchEngine,
                                    TagSetNotifier tagSetNotifier,
                                    IdentityProvider identityProvider) {

        this.tagSetFactory = tagSetFactory;
        this.tagSetRepository = tagSetRepository;
        this.tagSetDataSource = tagSetDataSource;
        this.tagSetSearchEngine = tagSetSearchEngine;
        this.tagSetNotifier = tagSetNotifier;
        this.identityProvider = identityProvider;
    }

    public TagSetResult createTagSet(CreateTagSetCommand createTagSetCommand) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User creator = new User(requesterIdentity.getId());

        TagSet newTagSet = tagSetFactory.create(
                creator,
                createTagSetCommand.name(),
                toTagIds(createTagSetCommand.tags())
        );
        tagSetRepository.add(newTagSet.forStorage());
        tagSetNotifier.notifyChanged(newTagSet.forChangeNotification());

        return tagSetDataSource.find(newTagSet.id(), creator)
                .map(this::toTagSetResult)
                .orElseThrow(() -> {
                    String message = "Could not find created tag set with ID=%s".formatted(newTagSet.id());
                    return new RuntimeException(message);
                });
    }

    public SearchResult<TagSetResult> searchTagSets(SearchTagSetsQuery searchTagSetsQuery) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User searcher = new User(requesterIdentity.getId());

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                searchTagSetsQuery.pageSize(),
                searchTagSetsQuery.pageId(),
                searchTagSetsQuery.term(),
                null,
                null,
                searchTagSetsQuery.excludeFilter(),
                null,
                null
        );
        EntitySearchResult<TagSetDto> searchResult = tagSetSearchEngine.findTagSets(searchCriteria);
        List<TagSetResult> resultForClient = searchResult.results()
                .stream()
                .map(this::toTagSetResult)
                .toList();

        return new SearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    public TagSetResult renameTagSet(String newName, UUID tagSetId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));
        TagSet tagSet = tagSetFactory.reconstitute(
                updater,
                new TagSetId(tagSetDto.id()),
                new User(tagSetDto.creatorId()),
                tagSetDto.name(),
                toTagIds(tagSetDto.tags()),
                tagSetDto.deleted()
        );
        tagSet.rename(newName, updater);
        tagSetRepository.update(tagSetId, tagSet.forStorage());

        tagSetNotifier.notifyChanged(tagSet.forChangeNotification());

        return tagSetDataSource.find(tagSet.id(), updater)
                .map(this::toTagSetResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag set with ID=%s".formatted(tagSet.id());
                    return new RuntimeException(message);
                });
    }

    public TagSetResult addTagToSet(UUID tagId, UUID tagSetId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));
        TagSet tagSet = tagSetFactory.reconstitute(
                updater,
                new TagSetId(tagSetDto.id()),
                new User(tagSetDto.creatorId()),
                tagSetDto.name(),
                toTagIds(tagSetDto.tags()),
                tagSetDto.deleted()
        );
        tagSet.assignTag(new TagId(tagId), updater);
        tagSetRepository.update(tagSetId, tagSet.forStorage());

        tagSetNotifier.notifyChanged(tagSet.forChangeNotification());

        return tagSetDataSource.find(tagSet.id(), updater)
                .map(this::toTagSetResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag set with ID=%s".formatted(tagSet.id());
                    return new RuntimeException(message);
                });
    }

    public TagSetResult removeTagFromSet(UUID tagId, UUID tagSetId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));
        TagSet tagSet = tagSetFactory.reconstitute(
                updater,
                new TagSetId(tagSetDto.id()),
                new User(tagSetDto.creatorId()),
                tagSetDto.name(),
                toTagIds(tagSetDto.tags()),
                tagSetDto.deleted()
        );

        tagSet.removeTag(new TagId(tagId), updater);
        tagSetRepository.update(tagSetId, tagSet.forStorage());

        tagSetNotifier.notifyChanged(tagSet.forChangeNotification());

        return tagSetDataSource.find(tagSet.id(), updater)
                .map(this::toTagSetResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag set with ID=%s".formatted(tagSet.id());
                    return new RuntimeException(message);
                });
    }

    public void deleteTagSet(UUID tagSetId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User remover = new User(requesterIdentity.getId());

        TagSetDto tagSetDto = tagSetRepository.findById(tagSetId)
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));
        TagSet tagSet = tagSetFactory.reconstitute(
                remover,
                new TagSetId(tagSetDto.id()),
                new User(tagSetDto.creatorId()),
                tagSetDto.name(),
                toTagIds(tagSetDto.tags()),
                tagSetDto.deleted()
        );

        tagSet.delete(remover);
        tagSetRepository.update(tagSetId, tagSet.forStorage());

        tagSetNotifier.notifyChanged(tagSet.forChangeNotification());
    }

    private TagSetResult toTagSetResult(TagSetDto tagSetResult) {
        return new TagSetResult(tagSetResult.id(), tagSetResult.name(), tagSetResult.tags());
    }

    private Set<TagId> toTagIds(Collection<UUID> uuids) {
        return uuids.stream()
                .map(TagId::new)
                .collect(toUnmodifiableSet());
    }
}
