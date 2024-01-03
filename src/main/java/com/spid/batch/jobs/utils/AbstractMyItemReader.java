package com.spid.batch.jobs.utils;

import com.oxit.spid.core.exceptions.FirstNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;

import java.io.IOException;
import java.util.List;

@Slf4j
public abstract class AbstractMyItemReader<T> extends AbstractItemStreamItemReader<T> {

    private List<T> list;

    protected abstract List<T> collectData() throws IOException, FirstNotFoundException;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            list = collectData();
        }
        catch (Exception e) {
            throw new ItemStreamException("Failed to initialize the reader", e);
        }
        log.debug("Reading {}", (list != null ? list.size() : "[null]"));
        super.open(executionContext);
    }

    @Override
    public void close() {
        super.close();
        if(list != null) {
            list.clear();
        }
    }

    @Override
    public T read() throws Exception {
        return this.list != null && !this.list.isEmpty() ? this.list.remove(0) : null;
    }
}
