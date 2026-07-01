import SwiftUI
import UniformTypeIdentifiers

struct AIQueryView: View {
    @State private var viewModel = AIQueryViewModel()
    @State private var prompt = ""
    @State private var selectedFileURL: URL?
    @State private var showFilePicker = false

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Picker("AI Provider", selection: $viewModel.selectedProviderIndex) {
                ForEach(Array(viewModel.providers.enumerated()), id: \.offset) { index, provider in
                    Text(provider.name).tag(index)
                }
            }

            Text("Prompt")
                .font(.headline)

            TextEditor(text: $prompt)
                .frame(minHeight: 160)
                .border(Color.secondary.opacity(0.3))

            VStack(alignment: .leading, spacing: 8) {
                Text("Attachment (Audio/Image/Video/Other media)")
                    .font(.headline)

                HStack {
                    Text(selectedFileURL?.lastPathComponent ?? "No file selected")
                        .foregroundStyle(.secondary)
                        .lineLimit(1)

                    Spacer()

                    Button("Choose File") {
                        showFilePicker = true
                    }

                    if selectedFileURL != nil {
                        Button("Clear") {
                            selectedFileURL = nil
                        }
                    }
                }
            }

            if case .error(let message) = viewModel.state {
                Text(message).foregroundStyle(.red).font(.caption)
            }

            if case .success(let fileURL) = viewModel.state {
                Text("Saved result to \(fileURL.lastPathComponent)")
                    .foregroundStyle(.green)
                    .font(.caption)
            }

            Button(action: {
                Task {
                    await viewModel.submit(prompt: prompt, attachmentURL: selectedFileURL)
                }
            }) {
                if case .sending = viewModel.state {
                    ProgressView().controlSize(.small)
                } else {
                    Text("Send to AI and Save Result")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(isSending)
        }
        .padding()
        .navigationTitle("AI Query")
        .fileImporter(
            isPresented: $showFilePicker,
            allowedContentTypes: [.item],
            allowsMultipleSelection: false
        ) { result in
            switch result {
            case .success(let urls):
                selectedFileURL = urls.first
                viewModel.resetState()
            case .failure:
                break
            }
        }
    }

    private var isSending: Bool {
        if case .sending = viewModel.state { return true }
        return false
    }
}
