package ru.practicum.shareit.pageable;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Value
@RequiredArgsConstructor
public class OffsetPageRequest implements Pageable {

    long offset;
    int limit;
    Sort sort;

    public OffsetPageRequest(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
        this.sort = Sort.unsorted();
    }

    @Override
    public int getPageNumber() {
        return (int) (offset / limit);
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public Pageable next() {
        return new OffsetPageRequest(offset + limit, limit, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        return new OffsetPageRequest(Math.max(offset - limit, 0), limit, sort);
    }

    @Override
    public Pageable first() {
        return new OffsetPageRequest(0, limit, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new OffsetPageRequest((long) pageNumber * limit, limit, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset >= limit;
    }
}
